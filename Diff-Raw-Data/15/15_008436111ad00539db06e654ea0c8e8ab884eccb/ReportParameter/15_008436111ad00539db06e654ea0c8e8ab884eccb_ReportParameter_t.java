 package com.spt.rms.rep.domain;
 
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.tostring.RooToString;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.util.Date;
 
 @XmlRootElement(name = "reportParameter")
 @RooJavaBean
 @RooToString
 @RooEntity(table = "RPTPARAM", finders = { "findReportParametersByReport", "findReportParametersByCode" })
 public class ReportParameter {
 
     @NotNull
     @Column(name = "RPPPARAM", unique = true)
     @Size(max = 15)
     private String code;
 
     @NotNull
     @Column(name = "RPPEDESC")
     @Size(max = 50)
     private String description;
 
     @Column(name = "RPPLENGTH")
     private Integer paramLength;
 
     @NotNull
     @Column(name = "RPPREQUIRE")
     private Integer paramRequire;
 
     @Column(name = "RPPDEFAULTVAL")
     @Size(max = 15)
     private String defaultValue;
 
     @NotNull
     @Column(name = "RPPCREUSR")
     @Size(max = 40)
     private String creUsr;
 
     @NotNull
     @Column(name = "RPPUPDUSR")
     @Size(max = 40)
     private String lastUsr;
 
     @NotNull
     @Column(name = "RPPCREDAT")
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(style = "S-")
     private Date creDate;
 
     @NotNull
     @Column(name = "RPPUPDDAT")
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(style = "S-")
     private Date lastDate;
 
     @NotNull
     @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RPPRPTID")
     private Report report;
 
     @Column(name = "RPPLOVSQL")
     @Size(max = 1000)
     private String query;
 
     @NotNull
     @Column(name = "RPPTYPE")
     @Size(max = 6)
     private String paramType;
 
     @NotNull
     @Column(name = "RPPLDESC")
     @Size(max = 50)
     private String localDescription;
 
     @NotNull
     @Column(name = "RPPHIDDEN")
     private Integer hidden;
 }
