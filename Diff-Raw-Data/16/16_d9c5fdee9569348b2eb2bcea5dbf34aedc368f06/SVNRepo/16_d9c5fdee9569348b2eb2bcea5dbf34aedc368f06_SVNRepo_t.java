 /*
  * [The "New BSD" license]
  * Copyright (c) 2012 The Board of Trustees of The University of Alabama
  * All rights reserved.
  *
  * See LICENSE for details.
  */

package edu.ua.eng.software.clonelink;

 import java.util.Collection;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNLogEntry;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
 
 /**
  * @author Paige Rodeghero <parodeghero@gmail.com>
  */
 public class SVNRepo {
     @SuppressWarnings("rawtypes")
 	public void crawl() {
         DAVRepositoryFactory.setup();
 
 		String url = "https://jhotdraw.svn.sourceforge.net/svnroot/jhotdraw";
 		long startingRevision = 0;
 		long endingRevision = -1;
 				
      try{
 		SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
 		Collection logEnteries = repository.log(new String[] {""}, null, startingRevision, endingRevision, true, true);
 		Pattern pattern = Pattern.compile(".*BUG.*", Pattern.CASE_INSENSITIVE);
 
 		for(Iterator entries = logEnteries.iterator();entries.hasNext();){
 			SVNLogEntry logEntry = (SVNLogEntry)entries.next();				
 			Matcher matcher = pattern.matcher(logEntry.toString());
 				 
 			if(matcher.find()){
 				System.out.println("Message Log: " + logEntry.getMessage());
                        }
                }
        }	
        catch (Exception e){
         }

	}

 }
