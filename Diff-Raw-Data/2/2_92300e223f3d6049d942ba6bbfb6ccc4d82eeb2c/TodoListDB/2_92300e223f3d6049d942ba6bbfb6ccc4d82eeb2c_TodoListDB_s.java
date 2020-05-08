 package com.intrbiz.balsa.demo.todo.db;
 
 import java.util.List;
 import java.util.UUID;
 
 import com.intrbiz.balsa.demo.todo.model.TodoList;
 import com.intrbiz.balsa.demo.todo.model.TodoListEntry;
 import com.intrbiz.data.DataException;
 import com.intrbiz.data.DataManager;
 import com.intrbiz.data.db.DatabaseAdapter;
 import com.intrbiz.data.db.DatabaseConnection;
 import com.intrbiz.data.db.compiler.DatabaseAdapterCompiler;
 import com.intrbiz.data.db.compiler.meta.SQLGetter;
 import com.intrbiz.data.db.compiler.meta.SQLOrder;
 import com.intrbiz.data.db.compiler.meta.SQLParam;
 import com.intrbiz.data.db.compiler.meta.SQLPatch;
 import com.intrbiz.data.db.compiler.meta.SQLQuery;
 import com.intrbiz.data.db.compiler.meta.SQLRemove;
 import com.intrbiz.data.db.compiler.meta.SQLSchema;
 import com.intrbiz.data.db.compiler.meta.SQLSetter;
 import com.intrbiz.data.db.compiler.meta.SQLVersion;
 import com.intrbiz.data.db.compiler.meta.ScriptType;
 import com.intrbiz.data.db.compiler.util.SQLScript;
 import com.intrbiz.metadata.ListOf;
 
 @SQLSchema(
         name = "todo", 
         version = @SQLVersion(major = 1, minor = 1),
         tables = {
             TodoList.class,
             TodoListEntry.class           
         }
 )
 public abstract class TodoListDB extends DatabaseAdapter
 {
     static
     {
        DataManager.getInstance().registerDatabaseAdapter2(TodoListDB.class, DatabaseAdapterCompiler.defaultPGSQLCompiler().compileAdapterFactory(TodoListDB.class));
     }
     
     public static final TodoListDB connect()
     {
         return DataManager.getInstance().databaseAdapter(TodoListDB.class);
     }
     
     public TodoListDB(DatabaseConnection connection)
     {
         super(connection);
     }
     
     public static void install()
     {
         DatabaseAdapterCompiler.defaultPGSQLCompiler("todo").install(DataManager.getInstance().connect(), TodoListDB.class);
     }
     
     // TodoList
     
     @SQLSetter
     public abstract void setTodoList(TodoList list) throws DataException;
     
     @SQLRemove
     public abstract void removeTodoList(TodoList list) throws DataException;
     
     @SQLGetter(orderBy = @SQLOrder("created"))
     @ListOf(TodoList.class)
     public abstract List<TodoList> getTodoLists() throws DataException;
     
     @SQLGetter
     public abstract TodoList getTodoList(@SQLParam("name") String name) throws DataException;
     
     // TodoListEntry
     
     @SQLSetter
     public abstract void setTodoListEntry(TodoListEntry entry) throws DataException;
     
     @SQLRemove
     public abstract void removeTodoListEntry(TodoListEntry entry) throws DataException;
     
     @SQLGetter
     public abstract TodoListEntry getTodoListEntry(@SQLParam("id") UUID id) throws DataException;
     
     @SQLGetter(orderBy = @SQLOrder("created"), query = @SQLQuery("SELECT * FROM todo.entry WHERE list_name = p_list_name AND complete = true"))
     @ListOf(TodoListEntry.class)
     public abstract List<TodoListEntry> getTodoListEntries(@SQLParam("list_name") String list) throws DataException;
     
     // patches
     
     // default values
     
     @SQLPatch(name = "Default task list", index = 1, type = ScriptType.INSTALL, version = @SQLVersion(major = 1, minor = 1))
     public static SQLScript defaultTaskList()
     {
         return new SQLScript(
                 "INSERT INTO todo.list (name, title, created) VALUES ('tasks', 'Tasks', now())",
                 "INSERT INTO todo.entry (id, list_name, title, created, complete, completed) VALUES ('" + UUID.randomUUID().toString() + "'::UUID,  'Try Balsa', NULL, now(), FALSE, NULL)"
         );
     }
 }
