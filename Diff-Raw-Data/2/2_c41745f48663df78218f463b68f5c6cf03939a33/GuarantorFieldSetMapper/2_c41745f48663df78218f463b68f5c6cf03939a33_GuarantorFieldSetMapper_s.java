 package tw.com.citi.cdic.batch.item.file.mapping;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.batch.item.file.mapping.FieldSetMapper;
 import org.springframework.batch.item.file.transform.FieldSet;
 
 import tw.com.citi.cdic.batch.model.Guarantor;
 
 /**
  * @author Chih-Liang Chang
  * @since 2011/10/3
  */
 public class GuarantorFieldSetMapper implements FieldSetMapper<Guarantor> {
 
     @Override
     public Guarantor mapFieldSet(FieldSet fieldSet) {
         Guarantor item = new Guarantor();
         item.setCustNo(customerNoTransformer(fieldSet.readString("custNo")));
         item.setUdf6(fieldSet.readString("udf6"));
         item.setUdf7(fieldSet.readString("udf7"));
         item.setUdf8(fieldSet.readString("udf8"));
         item.setUdf9(customerNoTransformer(fieldSet.readString("udf9")));
         item.setUdf10(fieldSet.readString("udf10"));
         try {
             item.setUdf11(Double.valueOf(fieldSet.readString("udf11")));
         } catch (NumberFormatException e) {
             item.setUdf11(0D);
         }
         item.setUdf12(fieldSet.readString("udf12"));
         item.setUdf13(charCodeTransformer(fieldSet.readString("udf13")));
         item.setUdf14(fieldSet.readString("udf14"));
         item.setUdf15(customerNoTransformer(fieldSet.readString("udf15")));
         item.setUdf16(fieldSet.readString("udf16"));
         item.setUdf17(fieldSet.readString("udf17"));
         try {
             item.setUdf18(Double.valueOf(fieldSet.readString("udf18")));
         } catch (NumberFormatException e) {
             item.setUdf18(0D);
         }
         item.setUdf19(fieldSet.readString("udf19"));
         item.setUdf20(charCodeTransformer(fieldSet.readString("udf20")));
         item.setUdf21(fieldSet.readString("udf21"));
         item.setUdf22(customerNoTransformer(fieldSet.readString("udf22")));
         item.setUdf23(fieldSet.readString("udf23"));
         item.setUdf24(fieldSet.readString("udf24"));
         try {
             item.setUdf25(Double.valueOf(fieldSet.readString("udf25")));
         } catch (NumberFormatException e) {
             item.setUdf25(0D);
         }
         item.setUdf26(fieldSet.readString("udf26"));
         item.setUdf27(charCodeTransformer(fieldSet.readString("udf27")));
         item.setUdf28(fieldSet.readString("udf28"));
         item.setUdf29(customerNoTransformer(fieldSet.readString("udf29")));
         item.setUdf30(fieldSet.readString("udf30"));
         item.setUdf31(fieldSet.readString("udf31"));
         try {
             item.setUdf32(Double.valueOf(fieldSet.readString("udf32")));
         } catch (NumberFormatException e) {
             item.setUdf32(0D);
         }
         item.setUdf33(fieldSet.readString("udf33"));
         item.setUdf34(charCodeTransformer(fieldSet.readString("udf34")));
         item.setUdf35(fieldSet.readString("udf35"));
         item.setUdf36(customerNoTransformer(fieldSet.readString("udf36")));
         item.setUdf37(fieldSet.readString("udf37"));
         item.setUdf38(fieldSet.readString("udf38"));
         try {
             item.setUdf39(Double.valueOf(fieldSet.readString("udf39")));
         } catch (NumberFormatException e) {
             item.setUdf39(0D);
         }
         item.setUdf40(fieldSet.readString("udf40"));
         item.setUdf41(charCodeTransformer(fieldSet.readString("udf41")));
         item.setUdf42(fieldSet.readString("udf42"));
         item.setUdf43(customerNoTransformer(fieldSet.readString("udf43")));
         item.setUdf44(fieldSet.readString("udf44"));
         item.setUdf45(fieldSet.readString("udf45"));
         try {
             item.setUdf46(Double.valueOf(fieldSet.readString("udf46")));
         } catch (NumberFormatException e) {
             item.setUdf46(0D);
         }
         item.setUdf47(fieldSet.readString("udf47"));
         item.setUdf48(charCodeTransformer(fieldSet.readString("udf48")));
         item.setUdf49(fieldSet.readString("udf49"));
         item.setUdf51(customerNoTransformer(fieldSet.readString("udf51")));
         item.setUdf52(fieldSet.readString("udf52"));
         item.setUdf53(fieldSet.readString("udf53"));
         try {
             item.setUdf54(Double.valueOf(fieldSet.readString("udf54")));
         } catch (NumberFormatException e) {
             item.setUdf54(0D);
         }
         item.setUdf55(fieldSet.readString("udf55"));
         item.setUdf56(charCodeTransformer(fieldSet.readString("udf56")));
         item.setUdf57(fieldSet.readString("udf57"));
         item.setUdf58(customerNoTransformer(fieldSet.readString("udf58")));
         item.setUdf59(fieldSet.readString("udf59"));
         item.setUdf60(fieldSet.readString("udf60"));
         try {
             item.setUdf61(Double.valueOf(fieldSet.readString("udf61")));
         } catch (NumberFormatException e) {
             item.setUdf61(0D);
         }
         item.setUdf62(fieldSet.readString("udf62"));
         item.setUdf63(charCodeTransformer(fieldSet.readString("udf63")));
         item.setUdf64(fieldSet.readString("udf64"));
         item.setUdf65(customerNoTransformer(fieldSet.readString("udf65")));
         item.setUdf66(fieldSet.readString("udf66"));
         item.setUdf67(fieldSet.readString("udf67"));
         try {
             item.setUdf68(Double.valueOf(fieldSet.readString("udf68")));
         } catch (NumberFormatException e) {
             item.setUdf68(0D);
         }
         item.setUdf69(fieldSet.readString("udf69"));
         item.setUdf70(charCodeTransformer(fieldSet.readString("udf70")));
         item.setUdf71(fieldSet.readString("udf71"));
         item.setUdf72(customerNoTransformer(fieldSet.readString("udf72")));
         item.setUdf73(fieldSet.readString("udf73"));
         item.setUdf74(fieldSet.readString("udf74"));
         try {
             item.setUdf75(Double.valueOf(fieldSet.readString("udf75")));
         } catch (NumberFormatException e) {
             item.setUdf75(0D);
         }
         item.setUdf76(fieldSet.readString("udf76"));
         item.setUdf77(charCodeTransformer(fieldSet.readString("udf77")));
         item.setUdf78(fieldSet.readString("udf78"));
         item.setUdf79(customerNoTransformer(fieldSet.readString("udf79")));
         item.setUdf80(fieldSet.readString("udf80"));
         item.setUdf81(fieldSet.readString("udf81"));
         try {
             item.setUdf82(Double.valueOf(fieldSet.readString("udf82")));
         } catch (NumberFormatException e) {
             item.setUdf82(0D);
         }
         item.setUdf83(fieldSet.readString("udf83"));
         item.setUdf84(charCodeTransformer(fieldSet.readString("udf84")));
         item.setUdf85(fieldSet.readString("udf85"));
         item.setUdf86(customerNoTransformer(fieldSet.readString("udf86")));
         item.setUdf87(fieldSet.readString("udf87"));
         item.setUdf88(fieldSet.readString("udf88"));
         try {
             item.setUdf89(Double.valueOf(fieldSet.readString("udf89")));
         } catch (NumberFormatException e) {
             item.setUdf89(0D);
         }
         item.setUdf90(fieldSet.readString("udf90"));
         item.setUdf91(charCodeTransformer(fieldSet.readString("udf91")));
         item.setUdf92(fieldSet.readString("udf92"));
         item.setUdf93(customerNoTransformer(fieldSet.readString("udf93")));
         item.setUdf94(fieldSet.readString("udf94"));
         item.setUdf95(fieldSet.readString("udf95"));
         try {
             item.setUdf96(Double.valueOf(fieldSet.readString("udf96")));
         } catch (NumberFormatException e) {
             item.setUdf96(0D);
         }
         item.setUdf97(fieldSet.readString("udf97"));
         item.setUdf98(charCodeTransformer(fieldSet.readString("udf98")));
         item.setUdf99(fieldSet.readString("udf99"));
         item.setUdf100(customerNoTransformer(fieldSet.readString("udf100")));
         item.setUdf101(fieldSet.readString("udf101"));
         item.setUdf102(fieldSet.readString("udf102"));
         try {
             item.setUdf103(Double.valueOf(fieldSet.readString("udf103")));
         } catch (NumberFormatException e) {
             item.setUdf103(0D);
         }
         item.setUdf104(fieldSet.readString("udf104"));
         item.setUdf105(charCodeTransformer(fieldSet.readString("udf105")));
         item.setUdf106(fieldSet.readString("udf106"));
         item.setUdf107(customerNoTransformer(fieldSet.readString("udf107")));
         item.setUdf108(fieldSet.readString("udf108"));
         item.setUdf109(fieldSet.readString("udf109"));
         try {
             item.setUdf110(Double.valueOf(fieldSet.readString("udf110")));
         } catch (NumberFormatException e) {
             item.setUdf110(0D);
         }
         item.setUdf111(fieldSet.readString("udf111"));
         return item;
     }
 
     private String customerNoTransformer(String customerNo) {
         if (customerNo == null) {
             return null;
         }
         if ("".equals(customerNo.trim())) {
             return null;
         }
         try {
             Integer.parseInt(customerNo.trim());
         } catch (NumberFormatException e) {
             return null;
         }
         customerNo = StringUtils.leftPad(customerNo, 9, "0");
         return customerNo;
     }
 
     private String charCodeTransformer(String charCode) {
         String result = charCode;
         if ("NA".equalsIgnoreCase(charCode)) {
            result = null;
         }
         return result;
     }
 
 }
