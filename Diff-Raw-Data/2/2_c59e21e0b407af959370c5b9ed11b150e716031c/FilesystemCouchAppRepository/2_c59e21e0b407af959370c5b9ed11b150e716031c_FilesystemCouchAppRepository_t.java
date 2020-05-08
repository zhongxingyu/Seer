 /**
  * Copyright (C) 2011 David Thomas Hume <dth at dthu.me>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.dthume.couchapp.model;
 
 import static org.apache.commons.io.FileUtils.readFileToString;
 import static org.apache.commons.io.FileUtils.writeStringToFile;
 import static org.apache.commons.lang3.StringUtils.isBlank;
 
 import static org.dthume.couchapp.model.CouchAppConstants.toId;
 import static org.dthume.couchapp.maven.util.IOUtil.iterateDirectories;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.jcouchdb.document.DesignDocument;
 import org.jcouchdb.document.View;
 
 public class FilesystemCouchAppRepository
 	implements CouchAppRepository {
 
 	private static abstract class IOWorker<R> {
 		abstract R executeInternal() throws IOException;
 		
 		R execute()
 		{
 			try
 			{
 				return executeInternal();
 			}
 			catch (IOException e)
 			{
 				throw new RuntimeException(e);
 			}
 		}
 	}
 	
 	private final File baseDir;
 	
 	public FilesystemCouchAppRepository(File baseDir) {
 		this.baseDir = baseDir;
 	}
 	
 	public Collection<String> listIds() {
 		final Set<String> ids = new HashSet<String>();
 		for (final File file : iterateDirectories(this.baseDir))
 			if (file.isDirectory())
 				ids.add(file.getName());
 		return ids;
 	}
 	
 	public DesignDocument create(DesignDocument app) {
 		this.baseDir.mkdirs();
 		return update(app);
 	}
 
 	public DesignDocument retrieve(final String id) {
 		return new IOWorker<DesignDocument>(){
 			DesignDocument executeInternal() throws IOException {
 				final DesignDocument document = read(id);
 				document.setId("_design/" + id);
 				return document;
 			}
 		}.execute();
 	}
 
 	public DesignDocument update(final DesignDocument app) {
 		return new IOWorker<DesignDocument>(){
 			DesignDocument executeInternal() throws IOException {
 				write(app);
 				return app;
 			}
 		}.execute();
 	}
 
 	public boolean delete(final DesignDocument app) {
 		return new IOWorker<Boolean>() {
 			public Boolean executeInternal() throws IOException {
 				return new File(baseDir, app.getId()).delete();
 			}
 		}.execute();
 	}
 	
 	private void write(DesignDocument doc) throws IOException {
 		final File app = new File(baseDir, toId(doc));
 		final File views = new File(app, CouchAppConstants.VIEWS_FILE);
 		views.mkdirs();
 		for (final Map.Entry<String, View> entry : doc.getViews().entrySet())
 			writeView(new File(views, entry.getKey()), entry.getValue());
 	}
 	
 	private void writeView(final File dir, final View view)
 			throws IOException {
 		dir.mkdir();
 		
 		if (!isBlank(view.getMap())) {
 			final File map = new File(dir, CouchAppConstants.MAP_FILE);
 			writeStringToFile(map, view.getMap());
 		}
 		
 		if (!isBlank(view.getReduce())) {
 			final File map = new File(dir, CouchAppConstants.REDUCE_FILE);
			writeStringToFile(map, view.getReduce());
 		}		
 	}
 	
 	private DesignDocument read(final String id) throws IOException {
 		final File app = new File(baseDir, id);
 		final File views = new File(app, CouchAppConstants.VIEWS_FILE);
 		final DesignDocument doc = new DesignDocument(id);
 		
 		if (views.exists())
 			for (final File viewFile : iterateDirectories(views))
 				doc.addView(viewFile.getName(), readView(viewFile));
 		
 		return doc;
 	}
 	
 	private View readView(final File viewFile) throws IOException {
 		final File mapFile =
 				new File(viewFile, CouchAppConstants.MAP_FILE);
 		final File reduceFile =
 				new File(viewFile, CouchAppConstants.REDUCE_FILE);
 		final String map = mapFile.exists() ?
 				readFileToString(mapFile) : "";
 		final String reduce = reduceFile.exists() ?
 				readFileToString(reduceFile) : "";
 		return new View(map, reduce);
 	}
 
 }
