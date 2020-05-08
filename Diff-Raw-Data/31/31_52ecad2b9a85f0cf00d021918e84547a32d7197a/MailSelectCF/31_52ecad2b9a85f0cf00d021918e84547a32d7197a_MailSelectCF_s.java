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
import com.atlassian.jira.issue.fields.config.FieldConfig;
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
         Set<String> cfVals;
         if (field.isGlobal())
         {
             cfVals = msMgr.getValues(Consts.GROBAL_CF_PROJ, field.getId());
         }
         else
         {
             if (issue == null || issue.getProjectObject() == null)
             {
                return super.getVelocityParameters(issue, field, fieldLayoutItem);
             }
             cfVals = msMgr.getValues(issue.getProjectObject().getKey(), field.getId());
         }
 
        Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);
         params.put("cfVals", cfVals);
         return params;
     }

    @Override
    public int compare(
        String customFieldObjectValue1,
        String customFieldObjectValue2,
        FieldConfig fieldConfig)
    {
        return super.compare(customFieldObjectValue1, customFieldObjectValue2, fieldConfig);
    }
 }
