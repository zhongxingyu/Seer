 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl;
 
 import java.io.*;
 
 public class DirectoryStore extends WritableStore {
 	
 	File root;
 	
 	DirectoryStore() throws IOException {
 		super();
 	};
 	
 	public DirectoryStore ( File dir ) throws IOException {
 		root = dir;
 		separator = File.separator;
 		initDir();
 		refresh();
 	}
 	
 	void initDir() throws IOException {
 		if ( ! root.exists() ){
 			if ( ! root.mkdirs() )
 				throw new IOException("Could not create directory '"+root.getPath()+"'");
 		} else if ( ! root.isDirectory() ) {
 			throw new IOException("Could not create dir "+root.getPath()+": a file with that name already exists.");
 		}
 	}
 	
 	void refresh() throws IOException {
		for ( File file : new Reflections(infoFile+"$",root).paths() )
 			try {
 				loadTrace(file.getParentFile().getName());
 			} catch (LoadTraceException e) {
 				System.err.println(e);
 			}
 	}
 	
 	@Override
 	public InputStream getInputStream(String name) throws IOException {
 		return new FileInputStream(new File(root,name));
 	}
 
 	@Override
 	public void deleteFile(String name) throws IOException {
 		File file = new File(root,name);
 		if ( ! file.delete() )
 			throw new IOException("Could not delete '"+file.getPath()+"'");
 	}
 
 	@Override
 	public void deleteTrace(String name) throws IOException {
 		File traceDir = new File(root, name);
 		rec_delete(traceDir);
 		traces.remove(name);
 	}
 
 	void rec_delete ( File file ) throws IOException {
 		if ( file.isDirectory() ){
 			for ( File f : file.listFiles() ){
 				rec_delete ( f );
 			}
 		} 
 		if ( ! file.delete() )
 			throw new IOException("Could not delete '"+file.getPath()+"'");
 	}
 
 	@Override
 	public OutputStream getOutputStream(String name) throws IOException {
 		File parent = new File(root,name).getParentFile();
 		parent.mkdirs();
 		return new FileOutputStream(new File(root,name));
 	}
 	
 	@Override
 	public boolean hasFile(String name) {
 		return new File(root,name).exists();
 	}
 
 }
