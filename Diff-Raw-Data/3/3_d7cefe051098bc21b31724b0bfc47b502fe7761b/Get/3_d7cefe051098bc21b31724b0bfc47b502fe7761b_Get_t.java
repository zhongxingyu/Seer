 /*
  * Copyright 1&1 Internet AG, http://www.1and1.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2 of the License,
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.sf.beezle.sushi.fs.webdav.methods;
 
 import net.sf.beezle.sushi.fs.webdav.StatusException;
 import net.sf.beezle.sushi.fs.webdav.WebdavConnection;
 import net.sf.beezle.sushi.fs.webdav.WebdavNode;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 
 import java.io.FileNotFoundException;
 import java.io.FilterInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 public class Get extends Method<InputStream> {
     public Get(WebdavNode resource) {
         super("GET", resource);
     }
 
     @Override
     public InputStream processResponse(final WebdavConnection connection, final HttpResponse response) throws IOException {
     	int status;
 
         status = response.getStatusLine().getStatusCode();
         switch (status) {
         case HttpStatus.SC_OK:
         	return new FilterInputStream(response.getEntity().getContent()) {
                 private boolean freed = false;
 
         		@Override
         		public void close() throws IOException {
                     if (!freed) {
                         freed = true;
                         root.free(response, connection);
                     }
                     super.close();
         		}
         	};
         case HttpStatus.SC_NOT_FOUND:
         case HttpStatus.SC_GONE:
         case HttpStatus.SC_MOVED_PERMANENTLY:
            root.free(response, connection);
             throw new FileNotFoundException(getUri());
         default:
            root.free(response, connection);
         	throw new StatusException(response.getStatusLine());
         }
     }
 
     @Override
     public void processResponseFinally(HttpResponse response, WebdavConnection connection) {
     	// do nothing - the resulting stream perform the close
     }
 }
