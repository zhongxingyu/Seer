 /**
  * 
  */
 
 package com.zygon.trade.modules.core;
 
 import com.zygon.trade.Module;
 import com.zygon.trade.ModuleProvider;
 import com.zygon.trade.db.Database;
 import com.zygon.trade.db.DatabaseFactory;
 
 /**
  *
  * @author zygon
  */
 public class CoreModuleProvider implements ModuleProvider {
 
     private final Module[] modules;
     
     public CoreModuleProvider() {
         UIModule uiModule = new UIModule("UI");
         
        Database db = DatabaseFactory.get("com.zygon.trade.db.hector.CassandraDatabase");
         DBModule dbModule = new DBModule(db);
         
         this.modules = new Module[]{ uiModule, dbModule };
     }
     
     @Override
     public Module[] getModules() {
         return this.modules;
     }
 }
