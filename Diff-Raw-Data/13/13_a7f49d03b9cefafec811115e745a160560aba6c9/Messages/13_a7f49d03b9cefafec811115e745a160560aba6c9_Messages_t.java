 /*
  * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
  
 package org.opensubsystems.core.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Class that represents collection of messages to report to the user. 
  * The messages might be grouped to categories which then determine how they are 
  * displayed to user. Category can represent the purpose of the message such as 
  * errors or informations or it can represent screen object such as control that 
  * contained data that caused the error or that user used to trigger the error.
  *  
  * @author bastafidli
  */
 public class Messages extends OSSObject
 {
    // Constants ////////////////////////////////////////////////////////////////
    
    /**
     * Key used to store nonspecific errors.
     */
    public static final String NONSPECIFIC_ERRORS = "NONSPECIFIC_ERRORS";
    
    /**
     * Key used to store nonspecific errors.
     */
    public static final String ACCESSRIGHT_ERRORS = "ACCESSRIGHT_ERRORS";
 
    /**
     * Key used to store all errors.
     */
    public static final String ALL_ERRORS = "ALL_ERRORS";
    
    /**
     * Key used to store all errors and info messages.
     */
    public static final String ALL = "ALL";
    
    /**
     * Key used to store informations not errors
     */
    public static final String INFORMATIONS = "INFORMATIONS";
 
    // Attributes ///////////////////////////////////////////////////////////////
 
    /**
     * Map which collects messages
     */
    protected Map<Object, List<String>> m_mpMessages;
    
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Construct new error object.
     */
    public Messages(
    )
    {
       m_mpMessages = new HashMap<>();
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
 
    /**
     * Add new nonspecific message to the list of error messages.
     * 
     * @param strMessage - message to add
     */
    public void addErrorMessage(
       String strMessage
    )
    {
       addMessage(NONSPECIFIC_ERRORS, strMessage);
    }
 
    /**
     * Add new message for a specific category. The category can represent for 
     * example dialog control for which the error message applies or one of the
     * constants defined in this class.
     * 
     * @param iMessageCategory - category code to which the message should be added
     * @param strMessage - message to add
     */   
    public void addMessage(
       int    iMessageCategory,
       String strMessage
    )
    {
       addMessage(new Integer(iMessageCategory), strMessage);
    }
    
    /**
     * Add new message for a specific category. The category can represent for 
     * example dialog control for which the error message applies or one of the
     * constants defined in this class.
     * 
     * @param messageCategory - category to which the message should be added
     * @param strMessage - message to add
     */   
    public void addMessage(
       Object messageCategory,
       String strMessage
    )
    {
       List<String> lstMessages;
       
       lstMessages = m_mpMessages.get(messageCategory);
       if (lstMessages == null)
       {
          lstMessages = new ArrayList<>();
          m_mpMessages.put(messageCategory, lstMessages);
       }
       if (!lstMessages.contains(strMessage))
       {
          lstMessages.add(strMessage);
       }
       
       // Now add the message to the all errors category in case we want all errors 
       if (!messageCategory.equals(INFORMATIONS))
       {
          lstMessages = m_mpMessages.get(ALL_ERRORS);
          if (lstMessages == null)
          {
             lstMessages = new ArrayList<>();
             m_mpMessages.put(ALL_ERRORS, lstMessages);
          }
          if (!lstMessages.contains(strMessage))
          {
             lstMessages.add(strMessage);
          }
       }
 
       // Now add the message to the all category in case we want all of them 
       lstMessages = m_mpMessages.get(ALL);
       if (lstMessages == null)
       {
          lstMessages = new ArrayList<>();
          m_mpMessages.put(ALL, lstMessages);
       }
       if (!lstMessages.contains(strMessage))
       {
          lstMessages.add(strMessage);
       }
    }
    
    /**
     * Method to merge Messages
     * 
     * @param erMessages - Messages to add
     */
    public void addMessages(
       Messages erMessages
    )
    {
       if (erMessages.m_mpMessages != null)
       {
         List<String> lstMessages;
          
          for (Map.Entry<Object, List<String>> item : erMessages.m_mpMessages.entrySet())
          {
            lstMessages = m_mpMessages.get(item.getKey());
             if (lstMessages != null)
             {
               lstMessages.addAll(item.getValue());
             }
             else
             {
                m_mpMessages.put(item.getKey(), item.getValue());
             }
          }
       }
    }
    
    /**
     * Returns list of all error messages.
     * 
     * @return List - list containing all error messages, may be null
     */
    // TODO: For Miro: Reexamine if we need this method
    public List<String> getAllErrorMessages(
    )
    {
       return getMessages(ALL_ERRORS);
    }
    
    /**
     * Returns list of all messages.
     * 
     * @return List - list containing all messages, may be null
     */
    // TODO: For Miro: Reexamine if we need this method
    public List<String> getAllMessages(
    )
    {
       return getMessages(ALL);
    }
    
    /**
     * Returns list of all info messages.
     * 
     * @return List - list containing all info messages, may be null
     */
    // TODO: For Miro: Reexamine if we need this method
    public List<String> getInfoMessages(
    )
    {
       return getMessages(INFORMATIONS);
    }
    
 
    /**
     * Returns list of all nonspecific messages.
     * 
     * @return List - list containing nonspecific messages, may be null
     */
    // TODO: For Miro: Reexamine if we need this method
    public List<String> getNonSpecificMessages(
    )
    {
       return getMessages(NONSPECIFIC_ERRORS);
    }
    
    /**
     * Returns String with all messages divided by delimiter.
     * 
     * @param strDelimiter - delimiter String
     * @return String - String containing messages for divided by delimiter, may 
     *                  be empty
     */
    // TODO: For Miro: Reexamine if we need this method
    public String getAllErrorMessages(
       String strDelimiter
    )
    {
       return getMessages(ALL_ERRORS, strDelimiter);
    }
 
    /**
     * Returns list of all messages for a given category.
     * 
     * @param messageCategory - messages for given category
     * @return List - list containing messages for given category, may be null
     */
    public List<String> getMessages(
       Object messageCategory
    )
    {
       return m_mpMessages.get(messageCategory);
    }
    
    /**
     * Returns String with all messages for a given category divided by delimiter.
     * 
     * @param messageCategory - messages for given category
     * @param strDelimiter - delimiter String
     * @return String - String containing messages for given category divided by 
     *                  delimiter, may be empty
     */
    public String getMessages(
       Object messageCategory,
       String strDelimiter
    )
    {
       StringBuilder sbHelp = new StringBuilder();
       
       // get all messages for category
       List<String> lstMessages = m_mpMessages.get(messageCategory);
 
       if (lstMessages != null && lstMessages.size() > 0)
       {
          for (String strMessage : lstMessages)
          {
             if (sbHelp.length() > 0 && strDelimiter != null)
             {
                sbHelp.append(strDelimiter);
             }
             sbHelp.append(strMessage);
          }
       }
       
       return sbHelp.toString();
    }
 
    /**
     * Test if the specified message is present in the specified category.
     * 
     * @param iMessageCategory - category code to which the message should be added
     * @param strMessage - message to add
     * @return boolean - true if given message is contained within given category.
     *                   false otherwise
     */
    public boolean containsMessage(
       int    iMessageCategory,
       String strMessage
    )
    {
       return containsMessage(new Integer(iMessageCategory), strMessage);
    }
    
    /**
     * Test if the specified message is present in the specified category.
     * 
     * @param messageCategory - category to which the message should be added
     * @param strMessage - message to add
     * @return boolean - true if given message is contained within given category.
     *                   false otherwise
     */
    public boolean containsMessage(
       Object messageCategory,
       String strMessage
    )
    {
      boolean      bReturn = false;
      List<String> lstMessages;
       
       lstMessages = m_mpMessages.get(messageCategory);
       if (lstMessages != null)
       {
          bReturn = lstMessages.contains(strMessage);
       }
 
       return bReturn;
    }
 }
