 /*
  * Copyright 2011 Tomas Schlosser
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
 package org.analyzer.html;
 
 import java.math.BigInteger;
 
 import org.analyzer.Element;
 
 
 
 public abstract class HtmlElement implements Element {
 	private final BigInteger id;
 	private final String name;
 	private final HtmlElement parent;
 	private final Attributes attributes;
 
 	private int position;
 	private String source;
 	private String textContent = "";
 
 	HtmlElement(
 			BigInteger id,
 			String name,
 			HtmlElement parent,
 			Attributes attributes) {
 		this(id, name, parent, attributes, null, 0);
 	}
 
 	private HtmlElement(
 			BigInteger id,
 			String name,
 			HtmlElement parent,
 			Attributes attributes,
 			String source,
 			int position) {
 		if (id == null) {
 			throw new NullPointerException("id can't be null");
 		}
 		if (name == null || name.length() < 1) {
 			throw new NullPointerException("name can't be null");
 		}
 		if (attributes == null) {
 			throw new NullPointerException("atributes can't be null");
 		}
 
 		this.id = id;
 		this.name = name;
 		this.parent = parent;
 		this.attributes = attributes;
 
 		this.source = source;
 		this.position = position;
 	}
 
 	public BigInteger getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public HtmlElement getParent() {
 		return parent;
 	}
 
 	public Attributes getAttributes() {
 		return attributes;
 	}
 
 	public void setSource(String source) {
		if (source == null || source.length() == 0) {
			throw new NullPointerException("source can't be null");
		}
 		this.source = source;
 	}
 
 	public String getSource() {
 		return source;
 	}
 
 	public void setPosition(int position) {
		if (position < 0) {
			throw new IllegalArgumentException("position must be greater then or equal to 0");
		}
 		this.position = position;
 	}
 
 	public int getPosition() {
 		return position;
 	}
 
 	public String getTextContent() {
 		return textContent;
 	}
 
 	public void setTextContent(String textContent) {
		if (textContent == null || textContent.length() == 0) {
			throw new NullPointerException("text content can't be null");
 		}

		this.textContent = textContent;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("%s[id='%s', position='%s', attributes='%s', source='%s']",
 				name, id, position, attributes, source);
 	}
 }
