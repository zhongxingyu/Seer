 package com.neutrino.models.configuration;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import play.data.format.Formats;
 import play.db.ebean.Model;
 
 import javax.annotation.Nullable;
 import javax.persistence.*;
 import java.math.BigDecimal;
 import java.util.*;
 import java.util.regex.Pattern;
 
 @Entity
 @Table(name = "MappingDiscoveryRule")
 public class MappingDiscoveryRule extends Model {
 
 	@Id
     @Column(name="ID")
     public Integer id;
 
     @Column(name="CoreTable", length = 64)
     public String coreTable;
 
     @Column(name="CoreColumn", length = 64)
     public String coreColumn;
 
     @Column(name="CoreType", length = 30)
     public String coreType;
 
     @Column(name="RefCode", length = 30)
     public String refCode;
 
     @Column(name="RefFullScore")
     public Integer refFullScore;
 
    @Column(name="RefMinimumPercMatch", precision = 3, scale=2)
     public BigDecimal refMinimumPercMatch;
 
    @Column(name="RefFullScorePercThresh", precision = 3, scale=2)
     public BigDecimal refFullScorePercThresh;
 
    @Column(name="NullMinimumPerc", precision = 3, scale=2)
     public BigDecimal nullMinimumPerc;
 
    @Column(name="NullMaximumPerc", precision = 3, scale=2)
     public BigDecimal nullMaximumPerc;
 
     @Column(name="NullPenalty")
     public Integer nullPenalty;
 
     @Column(name="NamePatterns", length = 256)
     public String namePatterns;
 
     @Column(name="NameScore")
     public Integer nameScore;
 
     @Column(name="MinimumValue", length = 30)
     public String minimumValue;
 
     @Column(name="MaximumValue", length = 30)
     public String maximumValue;
 
    @Column(name="AllowedExcPerc", precision = 3, scale=2)
     public BigDecimal allowedExcPerc;
 
     @Column(name="ValScore")
     public Integer valScore;
 
     @Column(name="valPenalty")
     public Integer valPenalty;
 
     @Column(name="FormatsCode", length = 30)
     public String formatsCode;
 
    @Column(name="FormatsMinimumPercThresh", precision = 3, scale=2)
     public BigDecimal formatsMinimumPercThresh;
 
     @Column(name="FormatsMetScore")
     public Integer formatsMetScore;
 
     @Column(name="MappingDiscoveryRulecol", length = 45)
     public String mappingDiscoveryRulecol;
 
     @Column(name="RegexPattern", length = 256)
     public String regexPattern;
 
    @Column(name="RegexMinimumPercThresh", precision = 3, scale=2)
     public BigDecimal regexMinimumPercThresh;
 
     @Column(name="RegexMetScore")
     public Integer regexMetScore;
 
     @Column(name="DataTypes", length = 30)
     public String dataTypes;
 
     @Column(name="MaybePointsThresh")
     public Integer maybePointsThresh;
 
     @Column(name="ConfPointsThresh")
     public Integer confPointsThresh;
 
     @Column(name="CreationTimestamp")
     @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
     public Date creationTimestamp;
 
     @Column(name="ModificationTimestamp")
     @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
     public Date modificationTimestamp;
 
     public static final Finder<Integer, MappingDiscoveryRule> find = new Finder<Integer, MappingDiscoveryRule>(
             Integer.class, MappingDiscoveryRule.class);
 
     @Transient
     private String _compiledFor_regexPattern;
     @Transient
     private Pattern _regexPatternCompiled;
     @Transient
     private String _compiledFor_namePatterns;
     @Transient
     private List<Pattern> _namePatternsCompiled;
 
     public Pattern regexPatternCompiled() {
         if (regexPattern == null || namePatterns.trim().isEmpty()) {
             return null;
         }
         if (_compiledFor_regexPattern == null || !_compiledFor_regexPattern.equals(regexPattern)) {
             _compiledFor_regexPattern = regexPattern;
             _regexPatternCompiled = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
         }
         return _regexPatternCompiled;
 
     }
 
     private List<Pattern> namePatternsCompiled() {
         if (namePatterns == null || namePatterns.trim().isEmpty()) {
             return Collections.EMPTY_LIST;
         }
         if (_compiledFor_namePatterns == null || !_compiledFor_namePatterns.equals(namePatterns)) {
             _compiledFor_namePatterns = namePatterns;
             List<String> tokens = Arrays.asList(namePatterns.split(","));
             _namePatternsCompiled = Lists.transform(tokens, new Function<String, Pattern>() {
                 @Nullable
                 @Override
                 public Pattern apply(@Nullable String s) {
                     String patternStr = s.trim().replaceAll("%", ".*?");
                     return Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
                 }
             });
 
         }
         return _namePatternsCompiled;
     }
 
     public boolean isNameMatching(String name) {
         for (Pattern p: namePatternsCompiled()) {
             if (p.matcher(name).find()) {
                 return true;
             }
         }
         return false;
     }
 }
