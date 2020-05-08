 package com.ryliu.j2ee.lab02;
 
 import java.io.IOException;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.TagSupport;
 
 public class Convert extends TagSupport {
 
     private static final long serialVersionUID = 1L;
 
     private String value;
 
     private final static char[] DIG = new char[] { '零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖' };
     private final static char[] UNIT = new char[] { '元', '拾', '百', '仟', '万', '亿' };
 
     @Override
     public int doEndTag() throws JspException {
         JspWriter writer = pageContext.getOut();
         try {
             writer.println(convert(value));
         } catch (IOException e) {
             e.printStackTrace(System.err);
         }
         return SKIP_BODY;
     }
 
     private static String convert(String str) {
        if (str.indexOf(".") == -1) {
             str += ".00";
         } else {
             str += "00";
         }
         String mm[] = str.split("\\.");
         String money = mm[0];
 
         StringBuilder resultBuilder = new StringBuilder();
         resultBuilder.append(DIG[mm[1].charAt(0) - '0']).append('角').append(DIG[mm[1].charAt(1) - '0']).append('分');
 
         int len = money.length();
         for (int i = 0; i < len; i++) {
             StringBuilder sb = new StringBuilder();
             int x = money.charAt(len - i - 1) - '0';
             sb.append(DIG[x]);
             if (i == 0) {
                 sb.append(UNIT[0]);
             } else if ((i + 4) % 8 == 0) {
                 sb.append(UNIT[4]);
             } else if (i % 8 == 0) {
                 sb.append(UNIT[5]);
             } else {
                 sb.append(UNIT[i % 4]);
             }
             resultBuilder.insert(0, sb);
         }
 
         String result = resultBuilder.toString();
         result = result.replaceAll("零([^亿万元角分])", "零");
         result = result.replaceAll("亿零+万", "亿零");
         result = result.replaceAll("零+", "零");
         result = result.replaceAll("零([亿万元])", "$1");
         result = result.replaceAll("壹拾", "拾");
         result = result.replaceAll("^元", "");
         result = result.replaceAll("零角零分", "整");
         result = result.replaceAll("零分", "整");
         return result;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public String getValue() {
         return value;
     }
 }
