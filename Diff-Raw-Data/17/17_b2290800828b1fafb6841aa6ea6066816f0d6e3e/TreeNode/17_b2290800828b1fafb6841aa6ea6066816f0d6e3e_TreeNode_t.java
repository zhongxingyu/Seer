 package com.ted.common.support.extjs4.tree;
 
 import java.io.Serializable;
 import java.util.Map;
 
 import com.google.common.collect.Maps;
 
 /**
  * 针对左边树的节点类做的映射类。
  */
 public class TreeNode implements Serializable {
     private static final long   serialVersionUID = 100L;
     //public String id; //id小心点设置，最好不要设置，用value，因为id重复了，折叠的时候会有问题//这个是Ext的主键
     private Long                id;                                  //这个是实体主键
     private Long                beanId;                              //=id,为了extjs4设计
     public String               text;                                //页面显示的字符 ,支持多语言？
     public boolean              leaf;
     public String               icon;
     public String               icon2;
     public String               icon3; //ghostzhang added 20121202
     public boolean              favorite;
    public boolean              separator; //means: hasSeparator
     public String               iconCls;
     private String              qtip;
     public boolean              singleClickExpand;
 
     //这个属性存放bean的一些附加信息。Dozer很好的解决了复制copy的问题。extjs 取的时候，node.attributes.beanMap.parentId
     private Map<String, Object> beanMap          = Maps.newHashMap();
 
     //比如Type 的code,属于node.attributes.value, 
     private String              code; 
     public String               path;                                //代替原来的href
     public String               type; //如果是MenuResource, code='menu', FileResource, code='file', PageResource ,code ='page',20130410 added this
 
    
    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public String getIcon3() {
 		return icon3;
 	}
 
 	public void setIcon3(String icon3) {
 		this.icon3 = icon3;
 	}
 
 	public String getIcon2() {
 		return icon2;
 	}
 
 	public void setIcon2(String icon2) {
 		this.icon2 = icon2;
 	}
 
 	public boolean isFavorite() {
 		return favorite;
 	}
 
 	public void setFavorite(boolean favorite) {
 		this.favorite = favorite;
 	}
 
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 
 	public String getIcon() {
 		return icon;
 	}
 
 	public void setIcon(String icon) {
 		this.icon = icon;
 	}
 
 	public Long getBeanId() {
         return beanId;
     }
 
     public void setBeanId(Long beanId) {
         this.beanId = beanId;
     }
 
     public String getPath() {
         return path;
     }
 
     public void setPath(String path) {
         this.path = path;
     }
 
     public String getCode() {
         return code;
     }
 
     public void setCode(String code) {
         this.code = code;
     }
 
     public Object getBeanMapValue(String key) {
         return beanMap.get(key);
     }
 
     public void setBeanMapValue(String key, Object value) {
         this.beanMap.put(key, value);
     }
 
     public Map<String, Object> getBeanMap() {
         return beanMap;
     }
 
     public void setBeanMap(Map<String, Object> beanMap) {
         this.beanMap = beanMap;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getQtip() {
         return qtip;
     }
 
     public void setQtip(String qtip) {
         this.qtip = qtip;
     }
 
     //public String getId() {
     //return id;
     //}
     //public void setId(String id) {
     //	this.id = id;
     //}
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     public boolean isLeaf() {
         return leaf;
     }
 
     public void setLeaf(boolean isLeaf) {
         this.leaf = isLeaf;
     }
 
     //	@JSON(serialize=false)
     //	public Node getParent() {
     //		return parent;
     //	}
     //	public void setParent(Node parent) {
     //		this.parent = parent;
     //	}
 
     public String getIconCls() {
         return iconCls;
     }
 
     public void setIconCls(String iconCls) {
         this.iconCls = iconCls;
     }
 
     public boolean getSingleClickExpand() {
         return singleClickExpand;
     }
 
     public void setSingleClickExpand(boolean singleClickExpand) {
         this.singleClickExpand = singleClickExpand;
     }
 }
