 package com.huanwuji.entity.bean;
 
 import com.huanwuji.core.jpa.TreeableEntityListener;
 import com.huanwuji.entity.BasicMethod;
 import com.huanwuji.entity.Treeable;
 import org.joda.time.DateTime;
 import org.springframework.data.annotation.CreatedDate;
 import org.springframework.data.annotation.LastModifiedDate;
 
 import javax.persistence.*;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * <p/>
  * User: juyee
  * Date: 12-7-31
  * Time: 下午5:09
  * To change this template use File | Settings | File Templates.
  */
 //菜单
 @Entity
 @EntityListeners(value = TreeableEntityListener.class)
 @Table(name = "MENU")
 public class Menu extends BasicMethod implements Treeable<Menu, Long> {
 
     @Column(name = "CODE", nullable = true, length = 30)
     private String code;
     @Column(name = "NAME", nullable = true, length = 50)
     private String name;
     @Column(name = "TITLE", nullable = true, length = 200)
     private String title;
     @Column(name = "leaf", nullable = true, length = 2)
     private Boolean leaf;
     @Column(name = "TREE_ID", nullable = true, length = 100)
     private String treeId;
     @Column(name = "DESCR", nullable = true, length = 500, columnDefinition = "TEXT")
     private String descr;
     @Column(name = "URL", nullable = true, length = 250)
     private String url;
     @Column(name = "ICON", nullable = true, length = 255)
     private String icon;
     @Column(name = "TYPE", nullable = true, length = 20)
     private String type;
     @Column(name = "VALID", nullable = true, length = 2)
     private Boolean valid;
 
     @CreatedDate
     @Column(name = "CREATE_DATE", nullable = true)
     private DateTime createDate;
 
     @LastModifiedDate
     @Column(name = "MODIFY_DATE", nullable = true)
     private DateTime modifyDate;
 
     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     @JoinColumn(name = "PARENT_ID", nullable = true, updatable = false)
     private Menu parent;
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
    private List<Menu> submenu;
 
     public Menu() {
     }
 
     public String getCode() {
         return code;
     }
 
     public void setCode(String code) {
         this.code = code;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public Boolean getLeaf() {
         return leaf;
     }
 
     public void setLeaf(Boolean leaf) {
         this.leaf = leaf;
     }
 
     public String getTreeId() {
         return treeId;
     }
 
     public void setTreeId(String treeId) {
         this.treeId = treeId;
     }
 
     public String getDescr() {
         return descr;
     }
 
     public void setDescr(String descr) {
         this.descr = descr;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public String getIcon() {
         return icon;
     }
 
     public void setIcon(String icon) {
         this.icon = icon;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public Boolean getValid() {
         return valid;
     }
 
     public void setValid(Boolean valid) {
         this.valid = valid;
     }
 
     public DateTime getCreateDate() {
         return createDate;
     }
 
     public void setCreateDate(DateTime createDate) {
         this.createDate = createDate;
     }
 
     public DateTime getModifyDate() {
         return modifyDate;
     }
 
     public void setModifyDate(DateTime modifyDate) {
         this.modifyDate = modifyDate;
     }
 
     public Menu getParent() {
         return parent;
     }
 
     public void setParent(Menu parent) {
         this.parent = parent;
     }
 
    public List<Menu> getSubmenu() {
        return submenu;
     }
 
    public void setSubmenu(List<Menu> submenu) {
        this.submenu = submenu;
     }
 }
