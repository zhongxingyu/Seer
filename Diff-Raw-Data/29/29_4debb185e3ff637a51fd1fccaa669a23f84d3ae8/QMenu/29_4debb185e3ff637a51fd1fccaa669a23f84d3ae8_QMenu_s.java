 package com.huanwuji.entity.query;
 
 import com.huanwuji.entity.bean.Menu;
 import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.*;
 
 import static com.mysema.query.types.PathMetadataFactory.forVariable;
 
 /**
  * description:.
  * User: huanwuji
  * create: 13-6-17 下午10:56
  */
 public class QMenu extends EntityPathBase<Menu> {
 
     private static final PathInits INITS = PathInits.DIRECT;
     public static final String ROOT = "menu";
 
     public static final String ID = "id";
     public static final String CODE = "code";
     public static final String NAME = "name";
     public static final String LEAF = "leaf";
     public static final String TREE_ID = "treeId";
     public static final String DESCR = "descr";
     public static final String URL = "url";
     public static final String ICON = "icon";
     public static final String TYPE = "type";
     public static final String VALID = "valid";
     public static final String PARENT = "parent";
    public static final String SUBMENU = "submenu";
 
     public static final QMenu MENU = new QMenu(ROOT);
     public final NumberPath<Long> id = createNumber(ID, Long.class);
     public final StringPath code = createString(CODE);
     public final StringPath name = createString(NAME);
     public final BooleanPath leaf = createBoolean(LEAF);
     public final StringPath treeId = createString(TREE_ID);
     public final StringPath descr = createString(DESCR);
     public final StringPath url = createString(URL);
     public final StringPath icon = createString(ICON);
     public final NumberPath<Integer> type = createNumber(TYPE, Integer.class);
     public final BooleanPath vaild = createBoolean(VALID);
     public final QMenu parent;
    public final ListPath<Menu, QMenu> submenu = createList("submenu", Menu.class, QMenu.class, PathInits.DEFAULT);
 
     public QMenu(String variable) {
         this(Menu.class, forVariable(variable), INITS);
     }
 
     public QMenu(BeanPath<? extends Menu> entity) {
         this(entity.getType(), entity.getMetadata(), INITS);
     }
 
     public QMenu(PathMetadata<?> metadata) {
         this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
     }
 
     public QMenu(PathMetadata<?> metadata, PathInits inits) {
         this(Menu.class, metadata, inits);
     }
 
     public QMenu(Class<? extends Menu> type, PathMetadata<?> metadata, PathInits inits) {
         super(type, metadata, inits);
 //        this.parent = inits.isInitialized(PARENT) ? new QMenu(forProperty("key")) : null;
         this.parent = inits.isInitialized(PARENT) ? new QMenu(forProperty(PARENT), inits.get(PARENT)) : null;
     }
 }
 
