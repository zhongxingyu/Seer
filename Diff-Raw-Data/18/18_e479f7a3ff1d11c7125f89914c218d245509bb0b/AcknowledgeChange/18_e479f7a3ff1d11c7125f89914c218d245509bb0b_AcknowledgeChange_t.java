 package com.todoroo.astrid.actfm.sync.messages;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
import android.util.Log;

 import com.todoroo.andlib.data.AbstractModel;
 import com.todoroo.astrid.core.PluginServices;
 import com.todoroo.astrid.dao.OutstandingEntryDao;
 
 public class AcknowledgeChange extends ServerToClientMessage {
 
    private static final String ERROR_TAG = "actfm-acknowledge-change"; //$NON-NLS-1$

     private final OutstandingEntryDao<?> dao;
 
     public AcknowledgeChange(JSONObject json) {
         super(json);
         String table = json.optString("table"); //$NON-NLS-1$
         if (NameMaps.SERVER_TABLE_TASKS.equals(table))
             dao = PluginServices.getTaskOutstandingDao();
         else if (NameMaps.SERVER_TABLE_TAGS.equals(table))
             dao = PluginServices.getTagOutstandingDao();
         else
             dao = null;
     }
 
     @Override
     public void processMessage() {
         JSONArray idsArray = json.optJSONArray("ids"); //$NON-NLS-1$
         if (idsArray != null && dao != null) {
             ArrayList<Long> idsList = new ArrayList<Long>();
             for (int i = 0; i < idsArray.length(); i++) {
                 try {
                     Long id = idsArray.getLong(i);
                     if (id <= 0)
                         continue;
 
                     idsList.add(id);
                 } catch (JSONException e) {
                    Log.e(ERROR_TAG, "Error getting long from " + idsArray + " at index " + i, e);  //$NON-NLS-1$//$NON-NLS-2$
                 }
             }
             dao.deleteWhere(AbstractModel.ID_PROPERTY.in(idsList.toArray(new Long[idsList.size()])));
         }
     }
 
 }
