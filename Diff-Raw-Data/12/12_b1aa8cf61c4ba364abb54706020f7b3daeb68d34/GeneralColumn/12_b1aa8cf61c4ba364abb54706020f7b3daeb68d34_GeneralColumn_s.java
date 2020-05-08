 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only
  *    (as Java .class files or a .jar file containing the .class files) and only
  *    as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software
  *    development kit, other library, or development tool without written consent of
  *    Netspective Corporation. Any modified form of The Software is bound by
  *    these same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective
  *    Corporation and may not be used to endorse products derived from The
  *    Software without without written consent of Netspective Corporation. "Sparx"
  *    and "Netspective" may not appear in the names of products derived from The
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind.
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: GeneralColumn.java,v 1.6 2003-04-02 22:53:23 shahid.shah Exp $
  */
 
 package com.netspective.commons.report.tabular.column;
 
 import java.text.Format;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.netspective.commons.report.tabular.calc.ColumnDataCalculator;
 import com.netspective.commons.report.tabular.TabularReport;
 import com.netspective.commons.report.tabular.TabularReportColumn;
 import com.netspective.commons.report.tabular.TabularReportColumnConditionalApplyFlag;
 import com.netspective.commons.report.tabular.TabularReportColumnConditionalState;
 import com.netspective.commons.report.tabular.TabularReportValueContext;
 import com.netspective.commons.report.tabular.calc.TabularReportCalcs;
 import com.netspective.commons.report.tabular.TabularReportColumnState;
 import com.netspective.commons.report.tabular.TabularReportDataSource;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.xml.template.TemplateConsumer;
 import com.netspective.commons.xml.template.TemplateConsumerDefn;
 
 public class GeneralColumn implements TabularReportColumn, TemplateConsumer
 {
     static public final String PLACEHOLDER_COLDATA = "{.}";
     static public final String PLACEHOLDER_OPEN = "{";
     static public final String PLACEHOLDER_CLOSE = "}";
 
     public static final String CTYPE_TEMPLATE_NAMESPACE = TabularReportColumn.class.getName();
     public static final String CTYPE_ATTRNAME_TYPE = "type";
     public static final String[] CTYPE_ATTRNAMES_SET_BEFORE_CONSUMING = null;
     public static final ColumnTypeTemplateConsumerDefn templateConsumer = new ColumnTypeTemplateConsumerDefn();
 
     protected static class ColumnTypeTemplateConsumerDefn extends TemplateConsumerDefn
     {
         public ColumnTypeTemplateConsumerDefn()
         {
             super(CTYPE_TEMPLATE_NAMESPACE, CTYPE_ATTRNAME_TYPE, CTYPE_ATTRNAMES_SET_BEFORE_CONSUMING);
         }
     }
 
     private int dataType;
     private int align;
     private int colIndex = -1;
     private ValueSource heading;
     private ValueSource headingAnchorAttrs;
     private ValueSource url;
     private ValueSource urlAnchorAttrs;
     private String calcCmd;
     private Format formatter;
     private String outputPattern;
     private int width;
     private long flags;
     private String breakHeader;
     private List conditionals;
     private NumberFormat generalNumberFmt = NumberFormat.getNumberInstance();
 
     public GeneralColumn()
     {
         setColIndex(-1);
     }
 
     public TemplateConsumerDefn getTemplateConsumerDefn()
     {
         return templateConsumer;
     }
 
     public final int getDataType()
     {
         return dataType;
     }
 
     public final void setDataType(int value)
     {
         dataType = value;
     }
 
     public final String getBreak()
     {
         return breakHeader;
     }
 
     public final void setBreak(String header)
     {
         breakHeader = header;
     }
 
     public final boolean isColIndexSet()
     {
         return colIndex != -1;
     }
 
     public final int getColIndex()
     {
         return colIndex;
     }
 
     public final void setColIndex(int value)
     {
         colIndex = value;
     }
 
     public final ValueSource getHeading()
     {
         return heading;
     }
 
     public void setHeading(ValueSource heading)
     {
         this.heading = heading;
     }
 
     public ValueSource getHeadingAnchorAttrs()
     {
         return headingAnchorAttrs;
     }
 
     public void setHeadingAnchorAttrs(ValueSource headingAnchorAttrs)
     {
         this.headingAnchorAttrs = headingAnchorAttrs;
     }
 
     public final ValueSource getUrl()
     {
         return url;
     }
 
     public void setUrl(ValueSource url)
     {
         this.url = url;
         if(url != null)
             setFlag(COLFLAG_WRAPURL);
         else
             clearFlag(COLFLAG_WRAPURL);
     }
 
     public ValueSource getUrlAnchorAttrs()
     {
         return urlAnchorAttrs;
     }
 
     public void setUrlAnchorAttrs(ValueSource urlAnchorAttrs)
     {
         this.urlAnchorAttrs = urlAnchorAttrs;
         if(urlAnchorAttrs != null)
             setFlag(COLFLAG_HAVEANCHORATTRS);
         else
             clearFlag(COLFLAG_HAVEANCHORATTRS);
     }
 
     public final int getWidth()
     {
         return width;
     }
 
     public final void setWidth(int value)
     {
         width = value;
     }
 
     public final int getAlign()
     {
         return align;
     }
 
     public final void setAlign(AlignStyle value)
     {
         align = value.getValueIndex();
     }
 
     public final long getFlags()
     {
         return flags;
     }
 
     public final boolean flagIsSet(long flag)
     {
         return (flags & flag) == 0 ? false : true;
     }
 
     public final void setFlag(long flag)
     {
         flags |= flag;
     }
 
     public final void clearFlag(long flag)
     {
         flags &= ~flag;
     }
 
     public final void updateFlag(long flag, boolean set)
     {
         if(set) flags |= flag; else flags &= ~flag;
     }
 
     public final String getCalcCmd()
     {
         return calcCmd;
     }
 
     public final void setCalc(String value)
     {
         calcCmd = value;
     }
 
     public final Format getFormatter()
     {
         return formatter;
     }
 
     public void setFormatter(Format value)
     {
         formatter = value;
     }
 
     public void setFormat(String value)
     {
         formatter = ReportColumnFactory.getFormat(value);
     }
 
     public final String getOutput()
     {
         return outputPattern;
     }
 
     public final void setOutput(String value)
     {
         outputPattern = value;
         if(outputPattern != null)
         {
             outputPattern = value;
             setFlag(COLFLAG_HASOUTPUTPATTERN);
         }
         else
             clearFlag(COLFLAG_HASOUTPUTPATTERN);
     }
 
     public String resolvePattern(String srcStr)
     {
         // find all occurrences of ${.} and replace with ${x} where x is the col index (array)
 
         int findLoc = srcStr.indexOf(PLACEHOLDER_COLDATA);
         if(findLoc == -1)
             return srcStr;
 
         setFlag(COLFLAG_HASPLACEHOLDERS);
 
         String replacedIn = srcStr;
         String replaceWith = PLACEHOLDER_OPEN + colIndex + PLACEHOLDER_CLOSE;
         while(findLoc >= 0)
         {
             StringBuffer sb = new StringBuffer(replacedIn);
             sb.replace(findLoc, findLoc + PLACEHOLDER_COLDATA.length(), replaceWith);
             replacedIn = sb.toString();
             findLoc = replacedIn.indexOf(PLACEHOLDER_COLDATA);
         }
         return replacedIn;
     }
 
     public String getFormattedData(TabularReportValueContext rc, TabularReportDataSource ds, int flags)
     {
         Object oData = ds.getActiveRowColumnData(rc, getColIndex(), flags);
         String data = oData == null ? "" : oData.toString();
         if((flags & TabularReportColumn.GETDATAFLAG_DO_CALC) != 0)
         {
             ColumnDataCalculator calc = rc.getCalc(getColIndex());
             if(calc != null)
                 calc.addValue(rc, this, ds, data);
         }
         return data;
     }
 
     public String getFormattedData(TabularReportValueContext rc, ColumnDataCalculator calc)
     {
         if(calc != null)
         {
             if(formatter != null)
                 return formatter.format(new Double(calc.getValue(rc)));
             else
                 return generalNumberFmt.format(calc.getValue(rc));
         }
         else
             return null;
     }
 
     public List getConditionals()
     {
         return conditionals;
     }
 
     public void importFromColumn(TabularReportColumn rc)
     {
         flags = rc.getFlags();
 
         this.heading = rc.getHeading();
         this.headingAnchorAttrs = rc.getHeadingAnchorAttrs();
         this.url = rc.getUrl();
         this.urlAnchorAttrs = rc.getUrlAnchorAttrs();
         this.conditionals = rc.getConditionals();
         setAlign(new AlignStyle(rc.getAlign()));
         setWidth(rc.getWidth());
         setCalc(rc.getCalcCmd());
         Format fmt = rc.getFormatter();
         if(fmt != null)
             setFormatter(fmt);
         setOutput(rc.getOutput());
     }
 
     public TabularReportColumnConditionalState createConditional()
     {
         return new TabularReportColumnConditionalApplyFlag();
     }
 
     public void addConditional(TabularReportColumnConditionalState state)
     {
         if(conditionals == null)
             conditionals = new ArrayList();
         conditionals.add(state);
     }
 
     public void setAllowSort(boolean flag)
     {
         if(flag)
             setFlag(TabularReportColumn.COLFLAG_SORT_ALLOWED);
         else
             clearFlag(TabularReportColumn.COLFLAG_SORT_ALLOWED);
     }
 
     public void setWordWrap(boolean flag)
     {
         if(flag)
             setFlag(TabularReportColumn.COLFLAG_NOWORDBREAKS);
         else
             clearFlag(TabularReportColumn.COLFLAG_NOWORDBREAKS);
     }
 
     public void setDisplay(boolean flag)
     {
         if(flag)
             setFlag(TabularReportColumn.COLFLAG_HIDDEN);
         else
             clearFlag(TabularReportColumn.COLFLAG_HIDDEN);
     }
 
     public void finalizeContents(TabularReport report)
     {
     }
 
     public TabularReportColumnState constructState(TabularReportValueContext rc)
     {
         return new GeneralColumnState(rc);
     }
 
     public class GeneralColumnState implements TabularReportColumnState
     {
         protected TabularReportValueContext rc;
         protected String heading;
         protected ColumnDataCalculator calc;
         protected long flags;
         protected String outputFormat;
         protected String url;
         protected String urlAnchorAttrs;
 
         protected GeneralColumnState(TabularReportValueContext rc)
         {
             this.rc = rc;
 
             ValueSource headingVs = GeneralColumn.this.getHeading();
             if(headingVs != null)
                 heading = headingVs.getValue(rc).getTextValue();
 
             String calcCmd = getCalcCmd();
             if(calcCmd != null)
             {
                 calc = TabularReportCalcs.getInstance().createDataCalc(calcCmd);
                 if(calc == null)
                     throw new RuntimeException("Unable to find calc '"+ calcCmd +"' in column " + getHeading());
             }
 
             flags = GeneralColumn.this.getFlags();
 
             if((flags & TabularReportColumn.COLFLAG_HIDDEN) == 0)
             {
                 if(flagIsSet(TabularReportColumn.COLFLAG_HASOUTPUTPATTERN))
                     outputFormat = resolvePattern(GeneralColumn.this.getOutput());
 
                 if(flagIsSet(TabularReportColumn.COLFLAG_WRAPURL))
                     url = resolvePattern(GeneralColumn.this.getUrl().getValue(rc).getTextValue());
 
                 if(flagIsSet(TabularReportColumn.COLFLAG_HAVEANCHORATTRS))
                     urlAnchorAttrs = resolvePattern(GeneralColumn.this.getUrlAnchorAttrs().getValue(rc).getTextValue());
                 else
                     urlAnchorAttrs = "";
             }
 
             if(flagIsSet(TabularReportColumn.COLFLAG_HAVECONDITIONALS))
             {
                 List conditionals = getConditionals();
                 for(int i = 0; i < conditionals.size(); i++)
                     ((TabularReportColumnConditionalState) conditionals.get(i)).makeStateChanges(rc, this);
             }
         }
 
         public final boolean isVisible()
         {
             return (flags & TabularReportColumn.COLFLAG_HIDDEN) == 0 ? true : false;
         }
 
         public final boolean isHidden()
         {
             return (flags & TabularReportColumn.COLFLAG_HIDDEN) == 0 ? false : true;
         }
 
         public final boolean haveCalc()
         {
             return calc != null;
         }
 
         public final ColumnDataCalculator getCalc()
         {
             return calc;
         }
 
         public final long getFlags()
         {
             return flags;
         }
 
         public final boolean flagIsSet(long flag)
         {
             return (flags & flag) == 0 ? false : true;
         }
 
         public final void setFlag(long flag)
         {
             flags |= flag;
         }
 
         public final void clearFlag(long flag)
         {
             flags &= ~flag;
         }
 
         public final void updateFlag(long flag, boolean set)
         {
             if(set) flags |= flag; else flags &= ~flag;
         }
 
         public final String getHeading()
         {
             return heading;
         }
 
         public final String getOutputFormat()
         {
             return outputFormat;
         }
 
         public final String getUrl()
         {
             return url;
         }
 
         public final String getUrlAnchorAttrs()
         {
             return urlAnchorAttrs;
         }
 
         public String getCssStyleAttrValue()
         {
             StringBuffer style = new StringBuffer();
             switch(getAlign())
             {
                 case TabularReportColumn.ALIGN_CENTER:
                     style.append("text-align: center;");
                     break;
 
                 case TabularReportColumn.ALIGN_RIGHT:
                     style.append("text-align: right;");
                     break;
             }
 
             if(flagIsSet(COLFLAG_NOWORDBREAKS))
                 style.append(" white-space: nowrap;");
 
             return style.toString();
         }
 
         public final void setHeading(String value)
         {
             heading = value;
         }
 
         public final void setOutputFormat(String value)
         {
             outputFormat = value;
         }
 
         public final void setUrl(String value)
         {
             url = value;
 
             if(value != null)
             {
                 setFlag(TabularReportColumn.COLFLAG_WRAPURL);
                 url = resolvePattern(value);
             }
             else
                 url = null;
         }
 
         public final void setUrlAnchorAttrs(String value)
         {
             urlAnchorAttrs = value;
             if(value != null)
             {
                 setFlag(TabularReportColumn.COLFLAG_HAVEANCHORATTRS);
                 urlAnchorAttrs = resolvePattern(value);
             }
             else
                 urlAnchorAttrs = "";
 
         }
     }
 }
