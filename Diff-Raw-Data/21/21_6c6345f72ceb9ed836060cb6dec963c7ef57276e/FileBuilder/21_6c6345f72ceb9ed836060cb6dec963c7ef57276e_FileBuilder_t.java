 /*
 	StatCvs - CVS statistics generation 
 	Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
 	http://statcvs.sf.net/
     
 	This library is free software; you can redistribute it and/or
 	modify it under the terms of the GNU Lesser General Public
 	License as published by the Free Software Foundation; either
 	version 2.1 of the License, or (at your option) any later version.
 
 	This library is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 	Lesser General Public License for more details.
 
 	You should have received a copy of the GNU Lesser General Public
 	License along with this library; if not, write to the Free Software
 	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
     
 	$RCSfile$
 	$Date$
 */
 package net.sf.statcvs.input;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 import net.sf.statcvs.model.CvsFile;
 
 /**
  * <p>Builds a {@link CvsFile} with {@link CvsRevision}s from logging data.
  * This class is responsible for deciding if a file or revisions will be
  * included in the report, for translating from CVS logfile data structures
  * to the data structures in the <tt>net.sf.statcvs.model</tt> package, and
  * for calculating the LOC history for the file.</p>
  * 
  * <p>A main goal of this class is to delay the creation of the <tt>CvsFile</tt>
  * object until all revisions of the file have been collected from the log.
  * We could simply create <tt>CvsFile</tt> and <tt>CvsRevision</tt>s on the fly
  * as we parse through the log, but this creates a problem if we decide not
  * to include the file after reading several revisions. The creation of a
  * <tt>CvsFile</tt> or <tt>CvsRevision</tt> can cause many more objects to
  * be created (<tt>Author</tt>, <tt>Directory</tt>, <tt>Commit</tt>), and
  * it would be very hard to get rid of them if we don't want the file. This
  * problem is solved by first collecting all information about one file in
  * this class, and then, with all information present, deciding if we want
  * to create the model instances or not.</p>
  * 
  * @author Richard Cyganiak <richard@cyganiak.de>
  * @author Tammo van Lessen
  * @version $Id$
  */
 public class FileBuilder {
 	private static Logger logger = Logger.getLogger(FileBuilder.class.getName());
 
 	private Builder builder;
 	private String name;
 	private boolean isBinary;
 	private List revisions = new ArrayList();
 	private RevisionData lastAdded = null;
     private Map revBySymnames;
 
 	private int locDelta;
 	/**
 	 * Creates a new <tt>FileBuilder</tt>.
 	 * 
 	 * @param builder a <tt>Builder</tt> that provides factory services for
 	 * author and directory instances and line counts.
 	 * @param name the filename
 	 * @param isBinary Is this a binary file or not?
 	 */
 	public FileBuilder(Builder builder,	String name, boolean isBinary, 
 						Map revBySymnames) {
 		this.builder = builder;
 		this.name = name;
 		this.isBinary = isBinary;
         this.revBySymnames = revBySymnames;
         
 		logger.fine("logging " + name);
 	}
 
 	/**
 	 * Adds a revision to the file. The revisions must be added in the
 	 * same order as they appear in the CVS logfile, that is, most recent
 	 * first.
 	 * 
 	 * @param data the revision
 	 */
 	public void addRevisionData(RevisionData data) {
 		if (!data.isOnTrunk()) {
 			return;
 		}
 		if (isBinary) {
 			data.setLines(0, 0);
 		}
 		this.revisions.add(data);
 		lastAdded = data;
 
 		locDelta += getLOCChange(data);
 	}
 	
 	/**
 	 * Creates and returns a {@link CvsFile} representation of the file.
 	 * <tt>null</tt> is returned if the file does not meet certain criteria,
 	 * for example if its filename meets an exclude filter or if it was dead
 	 * during the entire logging timespan.
 	 * 
 	 * @param beginOfLogDate the date of the begin of the log
 	 * @return a <tt>CvsFile</tt> representation of the file.
 	 */
 	public CvsFile createFile(Date beginOfLogDate) {
 		if (isFilteredFile() || !fileExistsInLogPeriod()) {
 			return null;
 		}
 		if (revisions.size() == 1 && lastAdded.isAddOnSubbranch()) {
 			return null;
 		}
 
 		CvsFile file = new CvsFile(name, builder.getDirectory(name));
 
 		if (revisions.isEmpty()) {
 			buildBeginOfLogRevision(file, beginOfLogDate, getFinalLOC(), null);
 			return file;
 		}
 
 		Iterator it = revisions.iterator();
 		RevisionData currentData = (RevisionData) it.next();
 		int currentLOC = getFinalLOC();
 		RevisionData previousData;
 		int previousLOC;
         SortedSet symbolicNames;
         
 		while (it.hasNext()) {
 			previousData = currentData;
 			previousLOC = currentLOC;
 			currentData = (RevisionData) it.next();
 			currentLOC = previousLOC - getLOCChange(previousData);
 
             // symbolic names for previousData
             symbolicNames = createSymbolicNamesCollection(previousData);
 
             if (previousData.isChangeOrRestore()) {
 				if (currentData.isDeletion() || currentData.isAddOnSubbranch()) {
 					buildCreationRevision(file, previousData, previousLOC, symbolicNames);
 				} else {
 					buildChangeRevision(file, previousData, previousLOC, symbolicNames);
 				}
 			} else if (previousData.isDeletion()) {
 				buildDeletionRevision(file, previousData, previousLOC, symbolicNames);
 			} else {
 				logger.warning("illegal state in "
 						+ file.getFilenameWithPath() + ":" + previousData.getRevisionNumber());
 			}
 		}
 
         // symbolic names for currentData
         symbolicNames = createSymbolicNamesCollection(currentData); 
 
 		int nextLinesOfCode = currentLOC - getLOCChange(currentData);
 		if (currentData.isCreation()) {
 			buildCreationRevision(file, currentData, currentLOC, symbolicNames);
 		} else if (currentData.isDeletion()) {
 			buildDeletionRevision(file, currentData, currentLOC, symbolicNames);
 			buildBeginOfLogRevision(file, beginOfLogDate, nextLinesOfCode, symbolicNames);
 		} else if (currentData.isChangeOrRestore()) {
 			buildChangeRevision(file, currentData, currentLOC, symbolicNames);
 			buildBeginOfLogRevision(file, beginOfLogDate, nextLinesOfCode, symbolicNames);
 		} else if (currentData.isAddOnSubbranch()) {
 			// ignore
 		} else {
 			logger.warning("illegal state in "
 					+ file.getFilenameWithPath() + ":" + currentData.getRevisionNumber());
 		}
 		return file;
 	}
 
 	/**
 	 * Gets a LOC count for the file's most recent revision. If the file
 	 * exists in the local checkout, we ask the {@link RepositoryFileManager}
 	 * to count its lines of code. If not (that is, it is dead), return
 	 * an approximated LOC value for its last non-dead revision.
 	 *  
 	 * @return the LOC count for the file's most recent revision.
 	 */
 	private int getFinalLOC() {
 		if (isBinary) {
 			return 0;
 		}
		else if (lastAdded != null && lastAdded.isAddOnSubbranch()) {
 			return locDelta;
 		}
 
 		String revision = null;
 		try {
 			revision = builder.getRevision(name);
 		}
 		catch (IOException e) {
 			if (!finalRevisionIsDead()) {
 				logger.warning(e.getMessage());
 			}
 		}
 			
 		try {
 			if ("1.1".equals(revision)) {
 				return builder.getLOC(name) + locDelta;
 			}
 			else {
				if (!revisions.isEmpty()) {
					RevisionData firstAdded = (RevisionData)revisions.get(0);
					if (!finalRevisionIsDead() && !firstAdded.getRevisionNumber().equals(revision)) {
						logger.warning("Revision of " + name + " does not match expected revision");
					}
 				}
 				return builder.getLOC(name);
 			}
 		} catch (NoLineCountException e) {
 			if (!finalRevisionIsDead()) {
 				logger.warning(e.getMessage());
 			}
 			return approximateFinalLOC();
 		}
 	}
 
 	/**
 	 * Returns <tt>true</tt> if the file's most recent revision is dead.
 	 * 
 	 * @return <tt>true</tt> if the file is dead.
 	 */
 	private boolean finalRevisionIsDead() {
 		if (revisions.isEmpty()) {
 			return false;
 		}
 		return ((RevisionData) revisions.get(0)).isDeletion();		
 	}
 
 	/**
 	 * Approximates the LOC count for files that are not present in the
 	 * local checkout. If a file was deleted at some point in history, then
 	 * we can't count its final lines of code. This algorithm calculates
 	 * a lower bound for the file's LOC prior to deletion by following the
 	 * ups and downs of the revisions.
 	 * 
 	 * @return a lower bound for the file's LOC before it was deleted
 	 */
 	private int approximateFinalLOC() {
 		int max = 0;
 		int current = 0;
 		Iterator it = revisions.iterator();
 		while (it.hasNext()) {
 			RevisionData data = (RevisionData) it.next();
 			current += data.getLinesAdded();
 			max = Math.max(current, max);
 			current -= data.getLinesRemoved();
 		}
 		return max;
 	}	
 
 	/**
 	 * Returns the change in LOC count caused by a revision. If there were
 	 * 10 lines added and 3 lines removed, 7 would be returned. This does
 	 * not take into account file deletion and creation.
 	 * 
 	 * @param data a revision
 	 * @return the change in LOC count
 	 */
 	private int getLOCChange(RevisionData data) {
 		return data.getLinesAdded() - data.getLinesRemoved();
 	}
 
 	private void buildCreationRevision(CvsFile file, RevisionData data, int loc, SortedSet symbolicNames) {
 		file.addInitialRevision(data.getRevisionNumber(),
 				builder.getAuthor(data.getLoginName()), data.getDate(),
 				data.getComment(), loc, symbolicNames);
 	}
 
 	private void buildChangeRevision(CvsFile file, RevisionData data, int loc, SortedSet symbolicNames) {
 		file.addChangeRevision(data.getRevisionNumber(),
 				builder.getAuthor(data.getLoginName()), data.getDate(),
 				data.getComment(), loc,
 				data.getLinesAdded() - data.getLinesRemoved(),
 				Math.min(data.getLinesAdded(), data.getLinesRemoved()), symbolicNames);	
 	}
 
 	private void buildDeletionRevision(CvsFile file, RevisionData data, int loc, SortedSet symbolicNames) {
 		file.addDeletionRevision(data.getRevisionNumber(),
 				builder.getAuthor(data.getLoginName()), data.getDate(),
 				data.getComment(), loc, symbolicNames);
 	}
 
 	private void buildBeginOfLogRevision(CvsFile file, Date beginOfLogDate, int loc, SortedSet symbolicNames) {
 		Date date = new Date(beginOfLogDate.getTime() - 60000);
 		file.addBeginOfLogRevision(date, loc, symbolicNames);
 	}
 
 	/**
 	 * Takes a filename and checks if it should be processed or not.
 	 * Can be used to filter out unwanted files.
 	 * 
 	 * @return <tt>true</tt> if this file should not be processed
 	 */
 	private boolean isFilteredFile() {
 		return name.startsWith("CVSROOT")
 				|| !builder.matchesPatterns(name);
 	}
 
 	/**
 	 * Returns <tt>false</tt> if the file did never exist in the timespan
 	 * covered by the log. For our purposes, a file is non-existant if it
 	 * has no revisions and does not exists in the module checkout.
 	 * Note: A file with no revisions
 	 * must be included in the report if it does exist in the module checkout.
 	 * This happens if it was created before the log started, and not changed
 	 * before the log ended.
 	 * @return <tt>true</tt> if the file did exist at some point in the log period.
 	 */
 	private boolean fileExistsInLogPeriod() {
 		if (revisions.size() > 0) {
 			return true;
 		}
 		try {
 			builder.getLOC(name);
 			return true;
 		} catch (NoLineCountException fileDoesNotExistInTimespan) {
 			return false;
 		}
 	}
     
     /**
      * Creates a sorted set containing all symbolic name objects affected by 
      * this revision.
      * If this revision has no symbolic names, this method returns null.
      * 
      * @param revisionData this revision
      * @return the sorted set or null
      */
     private SortedSet createSymbolicNamesCollection(RevisionData revisionData) 
     {
         SortedSet symbolicNames = null;
 
         Iterator symIt = revBySymnames.keySet().iterator();
         while (symIt.hasNext()) {
             String symName = (String)symIt.next();
             String rev = (String)revBySymnames.get(symName);
             if (revisionData.getRevisionNumber().equals(rev)) {
                 if (symbolicNames == null) {
                     symbolicNames = new TreeSet();
                 }
                 logger.fine("adding revision "+name+","+revisionData.getRevisionNumber()+" to symname "+symName);
                 symbolicNames.add(builder.getSymbolicName(symName));
             }
         }
         
         return symbolicNames;
     }
 }
