 /*
  * Copyright 2010 Jasha Joachimsthal
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
 package org.onehippo.forge.weblogdemo.components.overview;
 
 import org.hippoecm.hst.content.beans.standard.HippoBean;
 import org.hippoecm.hst.core.component.HstRequest;
 import org.onehippo.forge.weblogdemo.beans.Blogpost;
 
 import java.util.List;
 
 /**
  * Component for an overview of {@link org.onehippo.forge.weblogdemo.beans.Blogpost} documents.
 * {@inheritDoc}
 * 
  * @author Jasha Joachimsthal
  */
 public class BlogListing extends AbstractListing {
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected List<HippoBean> getDocumentsForOverview(HstRequest request, int page) {
         return getDocumentList(request, page, Blogpost.class);
     }
 
 
 }
