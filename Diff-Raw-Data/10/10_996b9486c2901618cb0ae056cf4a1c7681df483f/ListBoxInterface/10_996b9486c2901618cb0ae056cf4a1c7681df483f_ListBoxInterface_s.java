 
 package edu.common.dynamicextensions.domaininterface.userinterface;
 
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 
 /**
  * ListBoxInterface stores necessary information for generating ListBox control on
  * dynamically generated user interface.
  * @author geetika_bangard
  */
 public interface ListBoxInterface extends SelectInterface
 {
 
 	/**
 	 * This method returns whether the ListBox has a multiselect property or not.
 	 * @hibernate.property name="isMultiSelect" type="boolean" column="MULTISELECT"
 	 * @return whether the ListBox has a multiselect property or not.
 	 */
 	Boolean getIsMultiSelect();
 
 	/**
 	 * This method sets whether the ListBox has a multiselect property or not.
 	 * @param isMultiSelect the Boolean value indicating whether the ListBox has a multiselect property or not.
 	 */
 	void setIsMultiSelect(Boolean isMultiSelect);
 
 	/**
 	 * This method returns the Number of rows to be displayed on the UI for ListBox.
 	 * @return the Number of rows to be displayed on the UI for ListBox.
 	 */
 	Integer getNoOfRows();
 
 	/**
 	 * This method sets the Number of rows to be displayed on the UI for ListBox.
 	 * @param noOfRows the Number of rows to be set for ListBox.
 	 */
 	void setNoOfRows(Integer noOfRows);
 
 	/**
 	 *
 	 * This method returns AssociationInterface
 	 * @return association
 	 */
 	AssociationInterface getBaseAbstractAttributeAssociation();
 
 }
