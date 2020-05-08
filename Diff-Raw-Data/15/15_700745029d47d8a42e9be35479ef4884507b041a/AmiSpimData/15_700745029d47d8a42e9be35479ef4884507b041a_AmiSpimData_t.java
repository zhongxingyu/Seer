 /*
  * This file is part of OTC.
  * 
  * Copyright (c) 2011, Marius MOULIS <moulis.marius@gmail.com>
  *
  * OTC is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OTC is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OTC.  If not, see <http://www.gnu.org/licenses/>.
  */
 
  /** 
  * <p>
  * This class designs the AMISPIM data provided by XML parsing.
  * A datum could be the couple of a tag name and a value or 
  * a 3-tuple of a tag name, an attribute name and a value
  * </p>
  * 
  */
 package core.metadata;
 
 public class AmiSpimData {
 	
 	/**
 	 * 
 	 * Tag's name
 	 * 
 	 * */
 	private String tagName;
 	
 	/**
 	 * 
 	 * Attribute's name
 	 * 
 	 * */
 	private String attributeName;
 	
 	/**
 	 * 
 	 * Data value
 	 * 
 	 * */
 	private String value;
 	
 	/**
 	 * 
 	 * Used to know is the data is concerning a tag or a tag's attribute
 	 * 
 	 * */
 	private boolean isAttribute;
 	
 	/**
 	 * <p>
 	 * Used to know the position of this AMISPIM data if the considered meta data has duplicates in the OTC Template.<br/>
 	 * If the considered meta datum has duplicate in OTC Template file, the position will be used to know the right {@link AOTCMetadatum} which has to be filled.<br/>
 	 * See the AMISPIM Grammar for more informations.
 	 * </p> 
 	 * 
 	 * */
 	private int position;
 	
 	/**
 	 * 
 	 * As the position will change, this attribute will store the initial position
 	 * 
 	 * */
 	private final int inititalPosition;
 	
 	
 	/**
 	 * 
 	 * Construct a new AMISPIM datum concerning a tag
 	 *
 	 * @param tagName Name of the datum
 	 * @param value Value of the datum
 	 * @param position Position of the datum in OTC Template file
 	 *
 	 */
	public AmiSpimData(String tagName, int position, String value) {
 		this.tagName = tagName;
 		this.value = value;
 		this.attributeName = "";
 		this.isAttribute = false;
 		if (position >= 0)
 			this.position = position;
 		else
 			this.position = 0;
 		
 		this.inititalPosition = position;
 	}
 	
 	/**
 	 * 
 	 * Construct a new AMISPIM datum concerning an attribute
 	 *
 	 * @param tagName Name of the tag which owns the attribute
 	 * @param attributeName Name of the attribute
 	 * @param value Value of the attribute
  	 * @param position Position of the datum in OTC Template file
 	 *
 	 */
	public AmiSpimData(String attributeName, int position, String tagName, String value) {
 		this.tagName = tagName;
 		this.attributeName = attributeName;
 		this.value = value;
 		this.isAttribute = true;
 		if (position >= 0)
 			this.position = position;
 		else
 			this.position = 0;
 		
 		this.inititalPosition = position;
 
 	}
 
 	/**
 	 * 
 	 * Return the tag's name
 	 * @return Tag's name
 	 *
 	 */
 	public String getTagName() {
 		return tagName;
 	}
 
 	/**
 	 * 
 	 * Set a new tag name
 	 * @param tagName New tag name
 	 *
 	 */
 	public void setTagName(String tagName) {
 		this.tagName = tagName;
 	}
 
 	/**
 	 * 
 	 * Return the attribute's name
 	 * @return Attribute's name
 	 *
 	 */
 	public String getAttributeName() {
 		return attributeName;
 	}
 
 	/**
 	 * 
 	 * Set a new attribute's name
 	 * @param attributeName New attribute's name
 	 *
 	 */
 	public void setAttributeName(String attributeName) {
 		this.attributeName = attributeName;
 	}
 
 	/**
 	 * 
 	 * Get the value of this datum
 	 * @return Datum's value
 	 *
 	 */
 	public String getValue() {
 		return value;
 	}
 
 	/**
 	 * 
 	 * Set a new value for this datum
 	 * @param value New value for this datum
 	 *
 	 */
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	/**
 	 * 
 	 * Say if the datum is concerning an attribute
 	 * @return <b>true</b> if the data is concerning an attribute, <b>false</b> otherwise
 	 *
 	 */
 	public boolean isAttribute() {
 		return isAttribute;
 	}
 
 	/**
 	 * 
 	 * Set if the datum is concerning an attribute
 	 * @param isAttribute <b>true</b> if the data is concerning an attribute, <b>false</b> otherwise
 	 *
 	 */
 	public void setAttribute(boolean isAttribute) {
 		this.isAttribute = isAttribute;
 	}
 	
 	/**
 	 * 
 	 * Get the position of this AMIPSPIM datum
 	 * @return Position of this datum
 	 *
 	 */
 	public int getPosition() {
 		return position;
 	}
 
 	/**
 	 * 
 	 * Set the position of the AMIPSIM datum
 	 * @param position new position
 	 *
 	 */
 	public void setPosition(int position) {
 		this.position = position;
 	}
 
 	/**
 	 * <p>
 	 * Insert this AMISPIM value into the right {@link OTCMetadatum} <br/>
 	 * If the AMISPIM datum does not exist in the set of meta data generated by the Template, this function will return false.<br/>
 	 * In all other case, it will return true
 	 * </p>
 	 * @param mdg {@link OTCMetadataGroup} of meta data into which we want to insert the value
 	 * 
 	 * @return <b>true</b> if the AMISPIM value was corrected inserted, <b>false</b> otherwise.
 	 * 
 	 * */
 	public boolean insertValueInMetadatum(OTCMetadataGroup mdg){
 		
 		// Check in sub groups
 		for (OTCMetadataGroup subgroup : mdg.getSubGroups())
 			if (insertValueInMetadatum(subgroup))
 				return true;
 		
 		// Check in sub meta data
 		for (OTCMetadatum md : mdg.getSubMetadata())
 			if (md.getName().equals(this.tagName)){
 				if (position == 0){
 					md.setValue(this.value);
 					return true;
 				}
 				else {
 					position--;
 					continue;
 				}
 			}
 		
 		return false;
 	}
 		
 	/**
 	 * <p>
 	 * Insert this AMISPIM value into the right {@link OTCMetadataAttribute} <br/>
 	 * If the AMISPIM datum does not exist in the set of meta data generated by the Template, this function will return false.<br/>
 	 * In all other case, it will return true
 	 * </p>
 	 * @param mdg {@link OTCMetadataGroup} of meta data into which we want to insert the value
 	 * 
 	 * @return <b>true</b> if the AMISPIM value was corrected inserted, <b>false</b> otherwise.
 	 * 
 	 * */
 	public boolean insertValueInAttribute(OTCMetadataGroup mdg){
 		
 		// Check in sub groups
 		for (OTCMetadataGroup subgroup : mdg.getSubGroups())
 			if (insertValueInAttribute(subgroup))
 				return true;
 				
 		// Check in group attributes
 		for (OTCMetadataAttribute att : mdg.getAttributes())
 			if (mdg.getName().equals(tagName) && att.getName().equals(attributeName)){
 				if (position == 0){
 					att.setValue(this.value);
 					return true;
 				} else  {
 					position--;
 					continue;
 				}
 			}
 		
 		// Check in sub meta data attributes
 		for (OTCMetadatum md : mdg.getSubMetadata())
 			for (OTCMetadataAttribute att : md.getAttributes())
 				if (md.getName().equals(this.tagName) && att.getName().equals(attributeName)){
 					if (position == 0){
 						att.setValue(this.value);
 						return true;
 					} else {
 						position--;
 						continue;
 					}
 			}
 		
 		return false;
 	}
 	
 	@Override
 	public String toString(){
 		if (isAttribute)
 			return "Attribute Name : "+attributeName + "; Attribute Value : "+ value +"; Parent Tag : " + tagName + "; Position : " + inititalPosition;
 		else
 			return "Tag Name : " + tagName + "; Tag Value : "+ value + "; Position : " + inititalPosition;
 	}
 }
