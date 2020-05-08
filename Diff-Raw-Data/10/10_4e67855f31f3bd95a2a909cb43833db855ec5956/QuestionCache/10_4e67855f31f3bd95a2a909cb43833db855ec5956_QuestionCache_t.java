 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.qengine;
 
 import java.io.*;
 import java.net.URL;
 import java.util.*;
 import java.util.jar.JarFile;
 
 import om.OmDeveloperException;
 import om.OmException;
 import om.question.Question;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import util.misc.ClosableClassLoader;
 import util.misc.IO;
 import util.xml.XML;
 
 /**
  * Provides access to questions.
  * <p>
  * Note that all methods in here take a 'question key' parameter ({@link om.qengine.QuestionCache.QuestionKey});
  * this is a combination of the question ID and version.
  */
 public class QuestionCache
 {
 	/** Map of String (question key) -> QuestionStuff */
 	private Map<QuestionKey, QuestionStuff> mActiveQuestions1=new HashMap<QuestionKey, QuestionStuff>();
 
 	/** Map of Question -> String (question key) */
 	private Map<Question, QuestionKey> mActiveQuestions2=new HashMap<Question, QuestionKey>();
 
 	/** Folder where questions are cached */
 	private File fFolder;
 
 	QuestionCache(File f)
 	{
 		if(!f.exists()) f.mkdirs();
 		this.fFolder=f;
 	}
 
 	/** Holds stuff related to a particular question (ID) */
 	private static class QuestionStuff
 	{
		Class<?> c=null;
 		ClosableClassLoader ccl;
 		Document dMeta;
 		List<Question> lActive=new LinkedList<Question>();
 	}
 
 	/**
 	 * Questions are referenced in the cache by their ID and version. A question
 	 * key combines both these, with a full stop between.
 	 */
 	static class QuestionKey
 	{
 		private String sQuestionID,sVersion;
 
 		/**
 		 * @param sQuestionID ID of question
 		 * @param sVersion Version string
 		 */
 		QuestionKey(String sQuestionID,String sVersion)
 		{
 			this.sQuestionID=sQuestionID;
 			this.sVersion=sVersion;
 		}
 
 		// Hashcode and equals so it can be used in maps
 		@Override
 		public int hashCode()
 		{
 			return (sQuestionID+sVersion).hashCode();
 		}
 		@Override
 		public boolean equals(Object obj)
 		{
 			if(!(obj instanceof QuestionKey)) return false;
 			QuestionKey qkObj=(QuestionKey)obj;
 			return qkObj.sQuestionID.equals(sQuestionID) && qkObj.sVersion.equals(sVersion);
 		}
 
 		// For display in exceptions
 		@Override
 		public String toString()
 		{
 			return getURLPart();
 		}
 		/**
 		 * @return filename for cache
 		 */
 		public String getFileName()
 		{
 			return getURLPart()+".jar";
 		}
 		/**
 		 * @return url fragment for this question.
 		 */
 		public String getURLPart()
 		{
 			return sQuestionID+"."+sVersion;
 		}
 
 	}
 
 	/**
 	 * Returns metadata for a particular question ID. Must not be called unless
 	 * it is certain that the question is loaded (i.e. after newQuestion, before
 	 * returnQuestion).
 	 * @param qk Question key
 	 * @return Metadata
 	 * @throws OmException If question isn't loaded
 	 */
 	synchronized Document getMetadata(QuestionKey qk) throws OmException
 	{
 		checkNotShutdown();
 		// Find question ID in first map
 		QuestionStuff qs=mActiveQuestions1.get(qk);
 		if(qs==null)
 		{
 			// TODO Make it so it can cache the metadata somehow
 			// Load question temporarily just for metadata
 			File fJar=getFile(qk);
 			if(!fJar.exists()) throw new OmException("Question '"+qk+
 				"' does not exist");
 			try
 			{
 				JarFile jf=new JarFile(fJar);
 				InputStream is=jf.getInputStream(jf.getEntry("question.xml"));
 				Document dMeta=XML.parse(IO.loadBytes(is));
 				jf.close();
 				return dMeta;
 			}
 			catch(IOException ioe)
 			{
 				throw new OmException("Error loading question metadata for: "+qk,ioe);
 			}
 		}
 		return qs.dMeta;
 	}
 
 	/**
 	 * @param qk Question key
 	 * @return File that does/would correspond to that question
 	 */
 	private File getFile(QuestionKey qk)
 	{
 		return new File(fFolder,qk.getFileName());
 	}
 
 	/**
 	 * Checks whether a question is in the cache.
 	 * @param qk Question key
 	 * @return True if it's in the cache, false if it needs to be obtained
 	 *   and saved
 	 */
 	synchronized boolean containsQuestion(QuestionKey qk)
 	{
 		if(mActiveQuestions1==null) return false; // If shutdown
 
 		// If it's loaded in memory, we've got it, return true to save time
 		if(mActiveQuestions1.containsKey(qk)) return true;
 
 		// Otherwise see if it exixsts
 		return getFile(qk).exists();
 	}
 
 	/**
 	 * Saves a newly-retrieved question into the cache so that it can be loaded
 	 * with {@link #newQuestion(String)}.
 	 * @param qk Question key
 	 * @param abData Data of question .jar file
 	 * @throws OmException If there's an error saving it
 	 */
 	synchronized void saveQuestion(QuestionKey qk,byte[] abData)
 	  throws OmException
 	{
 		checkNotShutdown();
 		try
 		{
 			FileOutputStream fos=new FileOutputStream(getFile(qk));
 			fos.write(abData);
 			fos.close();
 		}
 		catch(IOException ioe)
 		{
 			throw new OmException("Failed to save question file",ioe);
 		}
 	}
 
 	static class QuestionInstance
 	{
 		Question q;
 		ClosableClassLoader ccl;
 	}
 
 	/**
 	 * Returns new instance of a question with the given question ID. Question
 	 * has not yet been initialised, only constructed.
 	 * <p>
 	 * The question jar file is loaded if necessary.
 	 * @param qk Question key
 	 * @return New Question object
 	 * @throws OmException If various problems with the .jar occur
 	 */
 	synchronized QuestionInstance newQuestion(QuestionKey qk) throws OmException
 	{
 		checkNotShutdown();
 		// Find question ID in first map
 		QuestionStuff qs=mActiveQuestions1.get(qk);
 		if(qs==null)
 		{
 			qs=new QuestionStuff();
 			mActiveQuestions1.put(qk,qs);
 		}
 
 		// If we don't already have the class loaded, we need to load it
 		if(qs.c==null)
 		{
 			File fJar=getFile(qk);
 			if(!fJar.exists()) throw new OmException("Question '"+qk+
 				"' does not exist");
 
 			// Get new classloader
 			try
 			{
 				qs.ccl=new ClosableClassLoader(fJar,getClass().getClassLoader());
 			}
 			catch(IOException ioe)
 			{
 				throw new OmException(
 					"Failed to start question classloader for: "+fJar,ioe);
 			}
 
 			boolean bSuccess=false;
 			try
 			{
 				// Get metadata document
 				try
 				{
 					URL uXML=qs.ccl.findResource("question.xml");
 					if(uXML==null)
 						throw new OmDeveloperException("question.xml not present in: "+fJar);
 					InputStream is=uXML.openStream();
 					qs.dMeta=XML.parse(is);
 					is.close();
 				}
 				catch(IOException ioe)
 				{
 					throw new OmDeveloperException(
 						"Failed to load or parse question.xml in: "+fJar,ioe);
 				}
 
 				// Find classname
 				Element eRoot=qs.dMeta.getDocumentElement();
 				if(!eRoot.getTagName().equals("question"))
 					throw new OmDeveloperException(
 						"Expecting <question> as root of question.xml in: "+fJar);
 				if(!eRoot.hasAttribute("class"))
 					throw new OmDeveloperException(
 						"Expecting class= attribute on root of question.xml in: "+fJar);
 				String sClass=eRoot.getAttribute("class");
 
 				// Load class
 				try
 				{
					qs.c = qs.ccl.loadClass(sClass);
 				}
 				catch(ClassNotFoundException cnfe)
 				{
 					throw new OmDeveloperException("Failed to find "+sClass+" in: "+fJar);
 				}
 
 				bSuccess=true;
 			}
 			finally
 			{
 				if(!bSuccess) qs.ccl.close();
 			}
 		}
 
 		// Instantiate question
 		Question q;
 		boolean bSuccess=false;
 		try
 		{
 			q=(Question)qs.c.newInstance();
 			bSuccess=true;
 		}
 		catch(ClassCastException cce)
 		{
 			throw new OmDeveloperException(
 				"Class "+qs.c+" doesn't implement Question, in: "+qk);
 		}
 		catch(InstantiationException ie)
 		{
 			throw new OmException(
 				"Error instantiating "+qs.c+" in: "+qk,ie);
 		}
 		catch(IllegalAccessException iae)
 		{
 			throw new OmException(
 				"Error instantiating "+qs.c+" (check it's public) in: "+qk,iae);
 		}
 		catch(Throwable t)
 		{
 			throw new OmException(
 				"Error instantiating "+qs.c+" in: "+qk,t);
 		}
 		finally
 		{
 			// If no other questions were already instantiated, throw away the
 			// classloader too
 			if(!bSuccess && qs.lActive.isEmpty())
 			{
 				mActiveQuestions1.remove(qk);
 				qs.ccl.close();
 			}
 		}
 
 		// Add question to both directional maps
 		qs.lActive.add(q);
 		mActiveQuestions2.put(q,qk);
 
 		// Return question
 		QuestionInstance qi=new QuestionInstance();
 		qi.q=q;
 		qi.ccl=qs.ccl;
 		return qi;
 	}
 
 	/**
 	 * Puts a question back in the bank once it's finished with. Call this
 	 * after you're sure there are no other references to the question. All
 	 * questions <strong>must</strong> be closed if newQuestion returned
 	 * successfully, otherwise it won't unload the jar file.
 	 * @param q Question to return to the bank.
 	 * @throws OmException If the data structures are inconsistent
 	 */
 	synchronized void returnQuestion(Question q) throws OmException
 	{
 		checkNotShutdown();
 		// Remove from both directional maps
 		QuestionKey qk=mActiveQuestions2.remove(q);
 		if(qk==null) throw new OmException(
 			"Attempt to close question that wasn't currently open");
 		QuestionStuff qs=mActiveQuestions1.get(qk);
 		if(qs==null)
 			throw new OmException("Question maps inconsistent (missing list)");
 		if(!qs.lActive.remove(q))
 			throw new OmException("Question maps inconsistent (missing question)");
 		if(!qs.lActive.isEmpty()) return; // If it wasn't the last, we're done
 
 		// Remove the whole question class entry from the jarfile
 		mActiveQuestions1.remove(qk);
 
 		// OK, that jar file can now be closed
 		qs.ccl.close();
 	}
 
 	/** Abandon all questions and close all classloaders */
 	synchronized void shutdown()
 	{
		for(QuestionStuff qs : mActiveQuestions1.values())
 		{
 			((ClosableClassLoader)qs.c.getClassLoader()).close();
 			qs.c=null;
 		}
 		mActiveQuestions1=null;
 		mActiveQuestions2=null;
 	}
 
 	/** @throws OmException If the question cache was shut down */
 	private void checkNotShutdown() throws OmException
 	{
 		if(mActiveQuestions1==null) throw new OmException(
 			"Can't call question cache methods after shutdown");
 	}
 
 }
