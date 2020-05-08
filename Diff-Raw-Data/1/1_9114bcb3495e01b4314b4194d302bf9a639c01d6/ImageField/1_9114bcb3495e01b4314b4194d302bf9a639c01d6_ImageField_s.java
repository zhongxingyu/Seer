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
 
 package burrito.client.crud.generic.fields;
 
 import burrito.client.crud.generic.CrudField;
 
 /**
  * A crud field representing an image. The way this {@link CrudField} is forced
  * is by using the following annotation on a {@link String} field in the entity:
  * <code>
  * <pre>@Image(width = 640, height = 360) public String image; </pre>
  * </code>
  * 
  * @author henper
  * 
  */
 @SuppressWarnings("serial")
 public class ImageField extends StringField {
 
 	int width;
 	int height;
 	boolean urlMode;
 	
 	public ImageField(String string, int width, int height, boolean urlMode) {
 		super(string);
 		this.width = width;
 		this.height = height;
 	}
 
 	public ImageField() {
 		// default constructor
 	}
 
 	/**
 	 * Gets the allowed width of the image
 	 * 
 	 * @return
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	/**
 	 * Gets the allowed height of the image
 	 * 
 	 * @return
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 	
 	/**
 	 * True if the field should store a relative URL
 	 * instead of a blob key
 	 * 
 	 * @return
 	 */
 	public boolean isUrlMode() {
 		return urlMode;
 	}
 
 	public void setUrlMode(boolean urlMode) {
 		this.urlMode = urlMode;
 	}
 }
