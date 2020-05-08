 /**
  * Copyright 2011 The Open Source Research Group,
  *                University of Erlangen-Nürnberg
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.fau.cs.osr.ptk.common.test;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import de.fau.cs.osr.utils.FmtIllegalArgumentException;
 
 public class ParserTestResources
 {
 	File baseDirectory;
 	
 	public ParserTestResources(File baseDirectory)
 	{
 		if (!baseDirectory.exists())
 			throw new FmtIllegalArgumentException(
 			        "Given base directory `%s' does not exist",
 			        baseDirectory);
 		
 		if (!baseDirectory.isDirectory())
 			throw new FmtIllegalArgumentException(
 			        "Given base directory `%s' is not a directory",
 			        baseDirectory);
 		
 		this.baseDirectory = baseDirectory;
 		
 	}
 	
 	public List<File> gather(String directory, String glob, final boolean recursive)
 	{
 		List<File> files = new LinkedList<File>();
 		
 		final Pattern p = Pattern.compile(glob);
 		
 		FilenameFilter filter = new FilenameFilter()
 		{
 			public boolean accept(File dir, String name)
 			{
 				Matcher matcher = p.matcher(name);
 				if (matcher.matches())
 					return true;
 				else if (recursive)
 					return new File(dir, name).isDirectory();
 				else
 					return false;
 			}
 		};
 		
 		File dir = new File(baseDirectory, directory);
 		
 		if (!dir.exists())
 			throw new FmtIllegalArgumentException(
 			        "Given directory `%s' does not exist",
 			        dir.getAbsolutePath());
 		
 		if (!dir.isDirectory())
 			throw new FmtIllegalArgumentException(
 			        "Given directory `%s' is not a directory",
 			        dir.getAbsolutePath());
 		
 		gatherRecursive(dir, filter, files);
 		Collections.sort(files);
 		
 		return files;
 	}
 	
	public String stripBaseDirectoryAndFixPath(String text)
 	{
		String p = Pattern.quote(baseDirectory.getAbsolutePath());
		Pattern rx = Pattern.compile(p + "(.*?:)");
		StringBuilder b = new StringBuilder();
		Matcher m = rx.matcher(text);
		
		int begin = 0;
		while (m.find(begin))
		{
			b.append(text.substring(begin, m.start()));
			b.append(m.group(1).replace(File.separatorChar, '/'));
			begin = m.end();
		}
		
		if (begin < text.length())
			b.append(text.substring(begin, text.length()));
		
		return b.toString();
 	}
 	
 	private static void gatherRecursive(File directory, FilenameFilter filter, List<File> gathered)
 	{
 		String[] files = directory.list(filter);
 		if (files != null)
 		{
 			for (String fileName : files)
 			{
 				File file = new File(directory, fileName);
 				
 				if (file.isDirectory())
 				{
 					gatherRecursive(file, filter, gathered);
 				}
 				else
 				{
 					gathered.add(file);
 				}
 			}
 		}
 	}
 	
 	public static File rebase(File file, String base, String rebase)
 	{
 		File rebasedFile = new File(
 		        file.getAbsolutePath().replace(base, rebase));
 		
 		if (!rebasedFile.exists())
 			throw new FmtIllegalArgumentException(
 			        "Rebased file `%s' does not exist",
 			        rebasedFile.getAbsolutePath());
 		
 		return rebasedFile;
 	}
 	
 	public static File rebase(File file, String base, String rebase, String ext)
 	{
 		return rebase(file, base, rebase, ext, false);
 	}
 	
 	public static File rebase(File file, String base, String rebase, String ext, boolean noThrow)
 	{
 		String rebased = file.getAbsolutePath().replace(base, rebase);
 		
 		int i = rebased.lastIndexOf('.');
 		rebased = i < 0 ?
 		        rebased + "." + ext :
 		        rebased.substring(0, i + 1) + ext;
 		
 		File rebasedFile = new File(rebased);
 		if (!rebasedFile.exists() && !noThrow)
 		{
 			throw new FmtIllegalArgumentException(
 			            "Rebased file `%s' does not exist",
 			            rebasedFile.getAbsolutePath());
 		}
 		
 		return rebasedFile;
 	}
 }
