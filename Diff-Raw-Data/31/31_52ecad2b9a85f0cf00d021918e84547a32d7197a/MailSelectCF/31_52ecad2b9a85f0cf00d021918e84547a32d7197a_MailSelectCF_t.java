 /*
  * Created by Andrey Markelov 29-08-2012.
  * Copyright Mail.Ru Group 2012. All rights reserved.
  */
 package ru.mail.plugins.ms;
 
 import java.util.Map;
 import java.util.Set;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.customfields.SortableCustomField;
 import com.atlassian.jira.issue.customfields.impl.TextCFType;
 import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
 import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
 import com.atlassian.jira.issue.fields.CustomField;
 import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
 
 /**
  * Select custom field implementation.
  * 
  * @author Andrey Markelov
  */
 public class MailSelectCF
     extends TextCFType
     implements SortableCustomField<String>
 {
     /**
      * Mail.Ru select manager.
      */
     private final MailSelectMgr msMgr;
 
     /**
      * Constructor.
      */
     public MailSelectCF(
         CustomFieldValuePersister customFieldValuePersister,
         GenericConfigManager genericConfigManager,
         MailSelectMgr msMgr)
     {
         super(customFieldValuePersister, genericConfigManager);
         this.msMgr = msMgr;
     }
 
     @Override
     public Map<String, Object> getVelocityParameters(
         Issue issue,
         CustomField field,
         FieldLayoutItem fieldLayoutItem)
     {
        Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);

         Set<String> cfVals;
         if (field.isGlobal())
         {
             cfVals = msMgr.getValues(Consts.GROBAL_CF_PROJ, field.getId());
         }
         else
         {
             if (issue == null || issue.getProjectObject() == null)
             {
                return params;
             }
             cfVals = msMgr.getValues(issue.getProjectObject().getKey(), field.getId());
         }
 
        String selectVal = "None";
        String value = (String)issue.getCustomFieldValue(field);
        for (String cf : cfVals)
        {
            selectVal = cf;
            if (value != null && cf.equals(value))
            {
                break;
            }
        }

        params.put("selectVal", selectVal);
         params.put("cfVals", cfVals);
         return params;
     }
 }
