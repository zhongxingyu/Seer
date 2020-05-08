 /**
  * A RESTful web service on top of DSpace.
  * Copyright (C) 2010-2011 National Library of Finland
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package fi.helsinki.lib.simplerest;
 
 import com.google.gson.Gson;
 import fi.helsinki.lib.simplerest.stubs.StubItem;
 import java.sql.SQLException;
 import java.util.HashSet;
 
 import org.dspace.core.Context;
 import org.dspace.content.WorkspaceItem;
 import org.dspace.content.InstallItem;
 import org.dspace.content.ItemIterator;
 
 import org.dspace.content.Collection;
 import org.dspace.content.Item;
 
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.ext.fileupload.RestletFileUpload;
 import org.restlet.representation.Representation;
 import org.restlet.representation.InputRepresentation;
 import org.restlet.resource.Get;
 import org.restlet.resource.Put; 
 import org.restlet.resource.Post;
 import org.restlet.resource.Delete;
 import org.restlet.resource.ResourceException;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.data.Method;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import org.apache.log4j.Logger;
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Priority;
 
 public class ItemsResource extends BaseResource {
 
     private static Logger log = Logger.getLogger(ItemResource.class);
     
     private Item[] items;
     private Context context;
     private int collectionId;
     
     public ItemsResource(Item[] items){
         this.items = items;
     }
     
     public ItemsResource(){
         this.items = null;
         try{
             this.context = new Context();
         }catch(SQLException e){
             log.log(Priority.FATAL, e);
         }
     }
 
 
     static public String relativeUrl(int collectionId) {
         return "collection/" + collectionId + "/items";
     }
     
     @Override
     protected void doInit() throws ResourceException {
         try {
             String s = (String)getRequest().getAttributes().get("collectionId");
             this.collectionId = Integer.parseInt(s);
         }
         catch (NumberFormatException e) {
             ResourceException resourceException =
                 new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                                       "Collection ID must be a number.");
             throw resourceException;
         }
     }
 
     @Get("html|xhtml|xml")
     public Representation toXml() {
         Collection collection = null;
         DomRepresentation representation = null;
         Document d = null;
         try {
             context = new Context();
             collection = Collection.find(context, this.collectionId);
             if (collection == null) {
                 return errorNotFound(context, "Could not find the collection.");
             }
 
             representation = new DomRepresentation(MediaType.TEXT_HTML);  
             d = representation.getDocument();  
         }
         catch (Exception e) {
             return errorInternal(context, e.toString());
         }
         
         Element html = d.createElement("html");  
         d.appendChild(html);
 
         Element head = d.createElement("head");
         html.appendChild(head);
 
         Element title = d.createElement("title");
         head.appendChild(title);
         title.appendChild(d.createTextNode("Items for collection " +
                                            collection.getName()));
 
         Element body = d.createElement("body");
         html.appendChild(body);
 	
         Element ulItems = d.createElement("ul");
         setId(ulItems, "items");
         body.appendChild(ulItems);
         
         String base = baseUrl();
         try {
             ItemIterator ii = collection.getItems();
             while (ii.hasNext()) {
                 Item item = ii.next();
                 Element li = d.createElement("li");
                 Element a = d.createElement("a");
                 String name = item.getName();
                 if (name == null) {
                     // FIXME: Should we really give names for items with no
                     // FIXME: name? (And if so does "Untitled" make sense?)
                     // FIXME: Anyway, this would break with null values.
                     name = "Untitled";
                 }
                 a.appendChild(d.createTextNode(name));
                 String href = base + ItemResource.relativeUrl(item.getID());
                 setAttribute(a, "href", href);
                 li.appendChild(a);
                 ulItems.appendChild(li);
             }
         }
         catch (SQLException e) {
             String errMsg =
                 "SQLException while trying to items of the collection. "+e.getMessage();
             return errorInternal(context, errMsg);
         }
 
         Element form = d.createElement("form");
         form.setAttribute("enctype", "multipart/form-data");
         form.setAttribute("method", "post");
         makeInputRow(d, form, "title", "Title");
         makeInputRow(d, form, "lang", "Language");
 
         Element submitButton = d.createElement("input");
         submitButton.setAttribute("type", "submit");
         submitButton.setAttribute("value", "Create a new item");
         form.appendChild(submitButton);
         
         body.appendChild(form);
 
         context.abort(); /* We did not make any changes to the database, so we could
                       call c.complete() instead (only it can potentially raise
                       SQLexception). */
 
         return representation;
     }
     
     @Get("json")
     public String toJson() throws SQLException{
         ItemIterator items;
         Collection collection = null;
         try{
             collection = Collection.find(context, collectionId);
             items = collection.getAllItems();
         }catch(Exception e){
             return errorInternal(context, e.toString()).getText();
         }
         
         Gson gson = new Gson();
         int itemSize = 0;
         while(items.hasNext()){
             itemSize++;
         }
         
         StubItem[] toJsonItems = new StubItem[itemSize];
         
         int i = 0;
         while(items.hasNext()){
             toJsonItems[i] = new StubItem(items.next());
             i++;
         }
         
         return gson.toJson(toJsonItems);   
     }
 
     @Put
     public Representation put(Representation dummy) {
         HashSet<Method> allowed = new HashSet();
         allowed.add(Method.GET);
         allowed.add(Method.POST);
         setAllowedMethods(allowed);
         return error(null,
                      "Items resource does not allow PUT method.",
                      Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
     }
 
     @Post
 	public Representation addItem(InputRepresentation rep) {
 	Collection collection;
 	try {
 	    context = getAuthenticatedContext();
 	    collection = Collection.find(context, this.collectionId);
 	    if (collection == null) {
 		return errorNotFound(context, "Could not find the collection.");
 	    }
 	}
 	catch (SQLException e) {
 	    return errorInternal(context, "SQLException");
 	}
 	String title = null;
 	String lang = null;
 
 	try {
 	    RestletFileUpload rfu =
 		new RestletFileUpload(new DiskFileItemFactory());
 	    FileItemIterator iter = rfu.getItemIterator(rep);
 
 	    while (iter.hasNext()) {
 		FileItemStream fileItemStream = iter.next();
 		if (fileItemStream.isFormField()) {
 		    String key = fileItemStream.getFieldName();
 		    String value =
 			IOUtils.toString(fileItemStream.openStream(), "UTF-8");
 
 		    if (key.equals("title")) {
 			title = value;
 		    }
 		    else if (key.equals("lang")) {
 			lang = value;
 		    }
 		    else if (key.equals("in_archive")) {
 			;
 		    }
 		    else if (key.equals("withdrawn")) {
 			;
 		    }
 		    else {
 			return error(context, "Unexpected attribute: " + key,
 				     Status.CLIENT_ERROR_BAD_REQUEST);
 		    }
 		}
 	    }
 	}
 	catch (Exception e) {
 	    return errorInternal(context, e.toString());
 	}
 
 	if (title == null) {
 	    return error(context, "There was no title given.",
 			 Status.CLIENT_ERROR_BAD_REQUEST);
 	}
 
 	Item item = null;
 	try {
 	    WorkspaceItem wsi = WorkspaceItem.create(context, collection, false);
 	    item = InstallItem.installItem(context, wsi);
 	    item.addMetadata("dc", "title", null, lang, title);
 	    item.update();
 	    context.complete();
 	}
 	catch (Exception e) {
            log.log(Priority.FATAL, e);
 	    return errorInternal(context, e.getMessage());
 	}
 
 	return successCreated("Created a new item.",
 			      baseUrl() +
 			      ItemResource.relativeUrl(item.getID()));
     }
 
     @Delete
     public Representation delete(Representation dummy) {
         HashSet<Method> allowed = new HashSet();
         allowed.add(Method.GET);
         allowed.add(Method.POST);
         setAllowedMethods(allowed);
         return error(null,
                      "Items resource does not allow DELETE method.",
                      Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
     }
 
 }
