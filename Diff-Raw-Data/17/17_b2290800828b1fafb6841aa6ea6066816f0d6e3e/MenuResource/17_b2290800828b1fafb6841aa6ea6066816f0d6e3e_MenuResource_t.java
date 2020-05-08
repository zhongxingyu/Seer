 package com.ted.xplatform.pojo.common;
 
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.Where;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 
 /**
  * 菜单资源
  * 
  * @version 1.0
  * @created 2012-03-01
  */
 @Entity
 //@Table(name = "menu_resource")
 @DiscriminatorValue("menu")
 public class MenuResource extends Resource {
     private static final long  serialVersionUID = -279141209449027079L;
     public static final String TYPE             = "menu";              //区别于FileResource, PageResource
 
     /**
      * 所属上级菜单
      */
     @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
     @JoinColumn(name = "parent_id")
     private MenuResource       parent;
 
     /**
      * 子菜单集合
      */
     @JsonIgnore
     @OneToMany(cascade = { CascadeType.DETACH }, mappedBy = "parent", fetch = FetchType.LAZY)
     // mappedBy="subMenuResources"
     @Where(clause = "deleted=0")
     // @JoinColumn(name = "parent_id")
     private List<MenuResource> subMenuResources;
 
     /**
      * 菜单路径
      */
     private String             path;
 
     /**
      * 索引号
      */
     private Integer            idx;
 
     /**
      * 修饰icon的cls,db中还多了一个冗余:cls,现在改为iconCls
      */
     public String              iconCls;
 
     public String              icon;                                   //最小的icon,for menu
 
     public String              icon2;                                   //for Short Cut 1
 
     public String              icon3;                                   //for Short Cut 2
 
     public boolean             favorite;                               // 是否是快捷方式
 
     /**
      * qtip，之所以命名为quicktip，因为在js文件中qtip有问题。
      */
     @Column(name = "qtip")
     private String             quicktip;
 
     /**
      * 是否是叶子
      */
     public boolean             leaf;
 
    @Column(name = "separators")
     public boolean             separator;                            //是否有分割线
 
     /**
      * 父亲Id,当初为啥要写一个呢？因为已经有了啊,这个是给新增的时候用的，要load然后把Menursource传过去，不过要到parentId，然后填写，把parentId保留。
      * 为保存所setParent()用。不过要先通过parentId load 出parent对象。
      */
     @Transient
     private Long               parentId;
 
     /**
      * 父亲菜单Name，注意是：Transient，不是给持久化用的，是给页面显示用的。
      */
     @Transient
     private String             parentName;
 
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
 
     public boolean isSeparator() {
         return separator;
     }
 
     public void setSeparator(boolean separator) {
         this.separator = separator;
     }
 
     public boolean isFavorite() {
         return favorite;
     }
 
     public void setFavorite(boolean favorite) {
         this.favorite = favorite;
     }
 
     public String getIcon() {
         return icon;
     }
 
     public void setIcon(String icon) {
         this.icon = icon;
     }
 
     @Transient
     public String getParentName() {
         return parentName;
     }
 
     public void loadParentName() {
         if (null != parent) {
             this.parentName = parent.getName();
         }
     }
 
     public void setParentName(String parentName) {
         this.parentName = parentName;
     }
 
     @JsonIgnore
     public MenuResource getParent() {
         return parent;
     }
 
     public void setParent(MenuResource parent) {
         this.parent = parent;
     }
 
     @JsonIgnore
     public List<MenuResource> getSubMenuResources() {
         return subMenuResources;
     }
 
     public void setSubMenuResources(List<MenuResource> subMenuResources) {
         this.subMenuResources = subMenuResources;
     }
 
     public String getPath() {
         return path;
     }
 
     public void setPath(String path) {
         this.path = path;
     }
 
     public Integer getIdx() {
         return idx;
     }
 
     public void setIdx(Integer idx) {
         this.idx = idx;
     }
 
     public String getIconCls() {
         return iconCls;
     }
 
     public void setIconCls(String iconCls) {
         this.iconCls = iconCls;
     }
 
     public String getQuicktip() {
         return quicktip;
     }
 
     public void setQuicktip(String quicktip) {
         this.quicktip = quicktip;
     }
 
     public boolean isLeaf() {
         return leaf;
     }
 
     public void setLeaf(boolean leaf) {
         this.leaf = leaf;
     }
 
     //	@Column(name = "parent_id")
     public Long getParentId() {
         if (getParent() == null) {
             return parentId;
         } else {
             return getParent().getId();
         }
     }
 
     //这个跟getParent不是1:1对应的，这个是给临时变量parentId赋值，getParentId()是通过parent对象获得parentId的。
     public void setParentId(Long parentId) {
         this.parentId = parentId;
     }
 
     //
     //	public void setParentId(Serializable parentId) {
     //		this.parentId = parentId;
     //	}
 
     public String getType() {
         return TYPE;
     }
 }
