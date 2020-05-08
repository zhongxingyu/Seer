 /* ************************************************************************** *
  * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 * 
 * This file is licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ************************************************************************** */
 package org.webreformatter.commons.uri;
 
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author kotelnikov
  */
 public class Uri extends AbstractUri {
 
     public static class Builder extends AbstractUriBuilder<Builder> {
 
         public Builder() {
             super();
         }
 
         public Builder(AbstractUri uri) {
             super(uri);
         }
 
         public Builder(String uri) {
             super(uri);
         }
 
         public Uri build() {
             return new Uri(this);
         }
 
         @Override
         protected Builder newCopy(AbstractUri uri) {
             return new Builder(uri);
         }
 
         public void setFullPath(String uri) {
             setFullPath(uri != null ? new Uri(uri) : null);
         }
 
     }
 
     public final static Uri EMPTY = new Uri("");
 
     private final String fFragment;
 
     private final String fHost;
 
     private final Path fPath;
 
     private final int fPort;
 
     private final List<QueryItem> fQueryItems;
 
     private final List<String> fSchemeSegments;
 
     private final String fUserInfo;
 
     public Uri(AbstractUri uri) {
         fPath = uri.getPath();
         fFragment = uri.getFragment();
         fHost = uri.getHost();
         fPort = uri.getPort();
        fQueryItems = Collections.unmodifiableList(uri.getQueryItems());
         fSchemeSegments = Collections.unmodifiableList(uri.getSchemeSegments());
         fUserInfo = uri.getUserInfo();
     }
 
     public Uri(String uri) {
         this(new Uri.Builder(uri));
     }
 
     @Override
     protected AbstractPath getAbstractPath() {
         return fPath;
     }
 
     public Uri.Builder getBuilder() {
         return new Builder(this);
     }
 
     @Override
     public String getFragment() {
         return fFragment;
     }
 
     @Override
     public String getHost() {
         return fHost;
     }
 
     public Uri getParent() {
         Path path = getPath();
         if (path.isEmpty()) {
             return this;
         }
         Builder builder = getBuilder();
         builder.getPathBuilder().getParent();
         return builder.build();
     }
 
     @Override
     public Path getPath() {
         return fPath;
     }
 
     public String getPath(boolean encode) {
         return fPath.getPath(encode);
     }
 
     public String getPath(boolean escape, boolean encode) {
         return fPath.getPath(escape, encode);
     }
 
     @Override
     public Path.Builder getPathBuilder() {
         return fPath.getBuilder();
     }
 
     @Override
     public int getPort() {
         return fPort;
     }
 
     @Override
     public List<QueryItem> getQueryItems() {
         return fQueryItems;
     }
 
     public Uri getRelative(AbstractUri anotherUri) {
         return getBuilder().getRelative(anotherUri).build();
     }
 
     public Uri getRelative(String anotherUri) {
         return getRelative(new Uri(anotherUri));
     }
 
     public Uri getResolved(AbstractPath relativePath) {
         Builder builder = getBuilder();
         builder.getResolved(relativePath);
         return builder.build();
     }
 
     public Uri getResolved(AbstractUri relativeUri) {
         Builder builder = getBuilder();
         builder.getResolved(relativeUri);
         return builder.build();
     }
 
     public Uri getResolved(String relativeUri) {
         return getResolved(new Uri(relativeUri));
     }
 
     @Override
     public List<String> getSchemeSegments() {
         return fSchemeSegments;
     }
 
     @Override
     public String getUserInfo() {
         return fUserInfo;
     }
 
 }
