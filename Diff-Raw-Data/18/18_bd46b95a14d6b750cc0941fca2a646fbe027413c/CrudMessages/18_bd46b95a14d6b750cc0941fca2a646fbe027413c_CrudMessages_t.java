 /**
  * Copyright 2011 Henric Persson (henric.persson@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package burrito.client.crud.labels;
 	
 import com.google.gwt.i18n.client.Messages;
 
 public interface CrudMessages extends Messages {
 
 	String entitiesRemoved(int count, String entity);
 
 	String deleteDescription();
 
 	String delete();
 	
 	String deleting();
 	
 	String edit();
 
 	String chooseEntity();
 	
 	String noEntitiesToEdit();
 
 	String admin();
 
 	String newEntity();
 
 	String uploadImage();
 
 	String removeImage();
 
 	String requiredImageWidth(int width);
 
 	String requiredImageSize(int width, int height);
 
 
 	String doUpload();
 
 	String uploadError(String clean);
 	String richText();
 
 	String rawHtml();
 
 	String minimize();
 
 	String maximize();
 
 	String link();
 
 	String pasteLinkHere();
 
 	String loading();
 
 	String bold();
 
 	String italic();
 
 	String strikethrough();
 
 	String underline();
 
 	String insertLink();
 
 	String insertImage();
 
 	String searchForEntity(String entity);
 
 	String search();
 
 	String clearSearchResults();
 
 	String maximumImageSize(int width, int height);
 
 	String siteletType();
 
 	String addSitelet();
 
 	String orderChanged();
 
 	String saveOrder();
 
 	String noSiteletsHaveBeenAdded();
 
 	String siteletsRemoved(int size);
 
 	String orderSaved();
 
 	String selectSiteletType();
 
 	String entityAddedAndCanBeSelected();
 	
 	String add();
 
 	
 	String clone();
 
 	String mustAddImage();
 
 	String noEmbeddedItemsAdded(String embeddedTypeInPlural);
 
 	String deleteEmbeddedItem();
 
 	String embeddedItemsDeleted(String embeddedTypeInPlural, int size);
 
 	String embeddedItemAtLeastOne(String embeddedTypeInSingular);
 
 	String linkIsRequired();
 
 	String chooseLinkType();
 
 	String createNewLink();
 
 	String linkText();
 
 	String selectLinkTo();
 
 	String writeOrPasteLink();
 
 	String save();
 
 	String cancel();
 
 	String selectLinkToEntity(String entityType);
 
 	String editLink();
 
 	String linkOutputAbsoluteUrl(String linkText, String absoluteLink);
 
 	String linkOutputUrl(String linkText, String typeLabel, String displayString);
 
 	String youCanSearchFor(String commaSeparateFields);
 
 	String clickToDeleteTag();
 
 	String confirmDeleteSitelet();
 
 	String selectOrChange(String entityType);
 
 	String selectEntity(String entityDisplayName);

	String searchListNoItemSelected();

	String searchListHelpText(String entityDisplayName);
 }
