 package com.lebk.dto;
 
 import java.util.Date;
 
 import com.lebk.dao.PtColorDao;
 import com.lebk.dao.PtSizeDao;
 import com.lebk.dao.PtTypeDao;
 import com.lebk.dao.impl.PtColorDaoImpl;
 import com.lebk.dao.impl.PtSizeDaoImpl;
 import com.lebk.dao.impl.PtTypeDaoImpl;
 import com.lebk.po.Product;
 
 /**
  * Author: lebk.lei@gmail.com Date: 2013-11-18
  */
 public class ProductDTO
 {
   private Integer id;
   private String name;
   private String ptType;
   private String ptColor;
   private String ptSize;
   private Integer ptNumber;
   private Date lastUpateTime;
   PtTypeDao ptd = new PtTypeDaoImpl();
   PtColorDao pcd = new PtColorDaoImpl();
   PtSizeDao psd = new PtSizeDaoImpl();
 
   public ProductDTO()
   {
 
   }
 
   public ProductDTO(Product p)
   {
     this.id = p.getId();
     this.name = p.getName();
     this.ptType = ptd.getNameByPtTypeId(p.getPtTypeId());
    this.ptColor = pcd.getColorNameByPtColorId(p.getPtColorId());
     this.ptSize = psd.getNameByPtSizeId(p.getPtSizeId());
     this.ptNumber = p.getPtNumber();
     this.lastUpateTime = p.getLastUpdateTime();
   }
 
   public Integer getId()
   {
     return id;
   }
 
   public void setId(Integer id)
   {
     this.id = id;
   }
 
   public String getName()
   {
     return name;
   }
 
   public void setName(String name)
   {
     this.name = name;
   }
 
   public String getPtType()
   {
     return ptType;
   }
 
   public void setPtType(String ptType)
   {
     this.ptType = ptType;
   }
 
   public String getPtSize()
   {
     return ptSize;
   }
 
   public void setPtSize(String ptSize)
   {
     this.ptSize = ptSize;
   }
 
   public String getPtColor()
   {
     return ptColor;
   }
 
   public void setPtColor(String ptColor)
   {
     this.ptColor = ptColor;
   }
 
   public Integer getPtNumber()
   {
     return ptNumber;
   }
 
   public void setPtNumber(Integer ptNumber)
   {
     this.ptNumber = ptNumber;
   }
 
   public Date getLastUpateTime()
   {
     return lastUpateTime;
   }
 
   public void setLastUpateTime(Date lastUpateTime)
   {
     this.lastUpateTime = lastUpateTime;
   }
 
 }
