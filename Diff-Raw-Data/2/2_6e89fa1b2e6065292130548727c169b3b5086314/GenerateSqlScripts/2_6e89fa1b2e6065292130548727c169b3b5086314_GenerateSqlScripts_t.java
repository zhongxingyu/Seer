 package org.wv.stepsovc.processors;
 import au.com.bytecode.opencsv.CSVReader;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 
 import static org.apache.commons.io.FileUtils.*;
 
 public class GenerateSqlScripts {
     
 
     public static void main(String a[]) {
 
         try {
             GenerateSqlScripts generateSqlScripts =new GenerateSqlScripts();
             //generateSqlScripts.generateScriptsFromCsv("/Users/balajig/Documents/Projects/Motech_Zambia/stepsovc-dmis-importer/src/main/resources/referral.csv","/Users/balajig/Documents/Projects/Motech_Zambia/stepsovc-dmis-importer/src/main/resources/insertScripts.sql");
             generateSqlScripts.generateScriptsFromCsv(a[0], a[1]);
         } catch (FileNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
     }
 
     private  void generateScriptsFromCsv(String csvFileName, String sqlFileName) throws IOException {
         CSVReader csvReader =new CSVReader(new FileReader(csvFileName), ',');
         StringBuffer stringBuffer =new StringBuffer();
         stringBuffer.append("BEGIN TRANSACTION\n");
         stringBuffer.append("BEGIN TRY\n");
         String elseExists="END\n" +
                 "ELSE\n" +
                 "BEGIN\n";
         String ifExistsEnd="END\n";
         csvReader.readNext();
         List<String[]> rowData =csvReader.readAll();
         for(String[] row : rowData){
             stringBuffer.append("IF EXISTS(Select 1 from tbl_referral_receipt  where rr_id='"+ row[0] +"')\n BEGIN\n");
             stringBuffer.append(generateUpdateScripts(row));
             stringBuffer.append(elseExists);
             stringBuffer.append(generateInsertScripts(row));
             stringBuffer.append("\r\n");
             stringBuffer.append(ifExistsEnd);
         }
         stringBuffer.append("COMMIT TRANSACTION\n");
         stringBuffer.append("END TRY\n");
         stringBuffer.append("BEGIN CATCH\n");
         stringBuffer.append("PRINT ERROR_MESSAGE();\n");
         stringBuffer.append("ROLLBACK TRANSACTION\n");
         stringBuffer.append("END CATCH\n");
         stringBuffer.append("GO");
 
         generateSQLFile(stringBuffer,sqlFileName);
      }
 
     private void generateSQLFile(StringBuffer sqlData,String sqlFileName){
         try {
             File file = new File(sqlFileName);
             writeStringToFile(file, sqlData.toString(), "UTF-8");
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 
     private String generateUpdateScripts(String[] row) {
          return "UPDATE tbl_referral_receipt " +
                  "SET [rr_referrer_name] ='"+row[1]+"'"+
                  ",[rr_referral_date] ='"+row[2]+"'"+
                  ",[rr_follow_up] ='"+row[3]+"'"+
                  ",[rr_signed] ='"+row[4]+"'"+
                  ",[rr_signed_date] ='"+row[5]+"'"+
                  ",[rr_hlt_art_rec] ="+row[6]+
                  ",[rr_hlt_art_ref] ="+row[7]+
                  ",[rr_hlt_condoms_rec] ="+row[8]+
                  ",[rr_hlt_condoms_ref] ="+row[9]+
                  ",[rr_hlt_ct_rec] ="+row[10]+
                  ",[rr_hlt_ct_ref] ="+row[11]+
                  ",[rr_hlt_diag_rec] ="+row[12]+
                  ",[rr_hlt_diag_ref] ="+row[13]+
                  ",[rr_hlt_fp_rec] ="+row[14]+
                  ",[rr_hlt_fp_ref] ="+row[15]+
                  ",[rr_hlt_hosp_rec] ="+row[16]+
                  ",[rr_hlt_hosp_ref] ="+row[17]+
                  ",[rr_hlt_other_hlt] ='"+row[18]+"'"+
                  ",[rr_hlt_other_hlt_rec] ="+row[19]+
                  ",[rr_hlt_other_hlt_ref] ="+row[20]+
                  ",[rr_hlt_pain_rec] ="+row[21]+
                  ",[rr_hlt_pain_ref] ="+row[22]+
                  ",[rr_hlt_pmtct_rec] ="+row[23]+
                  ",[rr_hlt_pmtct_ref] ="+row[24]+
                  ",[rr_hlt_sex_trans_rec] ="+row[25]+
                  ",[rr_hlt_sex_trans_ref] ="+row[26]+
                  ",[ben_id] ='"+row[27]+"'"+
                  ",[cg_id] ='"+row[28]+"'"+
                  ",[usr_update_id]='motech-stepsovc'"+
                  ",[usr_update_date]=GETDATE()"+
                  " WHERE rr_id='"+row[0]+"'\n";
     }
 
     public String generateInsertScripts(String row[]){
 
         return "INSERT INTO [STEPS_OVC_CHONGWE].[dbo].[tbl_referral_receipt]" +
                 "([rr_id]" +
                 ",[rr_referrer_name]" +
                 ",[rr_referral_date]" +
                 ",[rr_follow_up]" +
                 ",[rr_signed]" +
                 ",[rr_signed_date]" +
                 ",[rr_hlt_art_rec]" +
                 ",[rr_hlt_art_ref]" +
                 ",[rr_hlt_condoms_rec]" +
                 ",[rr_hlt_condoms_ref]" +
                 ",[rr_hlt_ct_rec]" +
                 ",[rr_hlt_ct_ref]" +
                 ",[rr_hlt_diag_rec]" +
                 ",[rr_hlt_diag_ref]" +
                 ",[rr_hlt_fp_rec]" +
                 ",[rr_hlt_fp_ref]" +
                 ",[rr_hlt_hosp_rec]" +
                 ",[rr_hlt_hosp_ref]" +
                 ",[rr_hlt_other_hlt]" +
                 ",[rr_hlt_other_hlt_rec]" +
                 ",[rr_hlt_other_hlt_ref]" +
                 ",[rr_hlt_pain_rec]" +
                 ",[rr_hlt_pain_ref]" +
                 ",[rr_hlt_pmtct_rec]" +
                 ",[rr_hlt_pmtct_ref]" +
                 ",[rr_hlt_sex_trans_rec]" +
                 ",[rr_hlt_sex_trans_ref]" +
                 ",[ben_id]" +
                 ",[cg_id]" +
                 ",[rr_care_name]" +
                 ",[rr_eco_strength_name]" +
                 ",[rr_edu_name]" +
                 ",[rr_food_other]" +
                 ",[rr_legal_other]" +
                 ",[rr_psych_other_psych]" +
                 ",[rr_receive_org]" +
                 ",[rr_receiver_title]" +
                 ",[rr_referrer_designation]" +
                 ",[rr_other_name]" +
                 ",[rr_care_rec]" +
                 ",[rr_care_ref]" +
                 ",[rr_eco_strength_rec]" +
                 ",[rr_eco_strength_ref]" +
                 ",[rr_edu_rec]" +
                 ",[rr_edu_ref]" +
                 ",[rr_food_coun_rec]" +
                 ",[rr_food_coun_ref]" +
                 ",[rr_food_other_rec]" +
                 ",[rr_food_other_ref]" +
                 ",[rr_food_sup_rec]" +
                 ",[rr_food_sup_ref]" +
                 ",[rr_legal_abuse_rec]" +
                 ",[rr_legal_abuse_ref]" +
                 ",[rr_legal_other_rec]" +
                 ",[rr_legal_other_ref]" +
                 ",[rr_other_rec]" +
                 ",[rr_other_ref]" +
                 ",[rr_psych_coun_rec]" +
                 ",[rr_psych_coun_ref]" +
                 ",[rr_psych_other_psych_rec]" +
                 ",[rr_psych_other_psych_ref]" +
                 ",[rr_psych_other_sup_rec]" +
                 ",[rr_psych_other_sup_ref]" +
                 ",[rr_psych_pos_liv_rec]" +
                 ",[rr_psych_pos_liv_ref]" +
                 ",[rr_psych_spiritual_rec]" +
                 ",[rr_psych_spiritual_ref]" +
                 ",[rr_deleted]," +
                 "[usr_create_id]," +
                 "[usr_update_id]," +
                 "[usr_create_date]," +
                "[usr_update_date]" +
                 ")" +
                 "VALUES" +
                 "('"+row[0]+"','"+row[1]+"','"+row[2]+"','"+row[3]+"','"+row[4]+"','"+row[5]+"',"+row[6]+","+row[7]+","+row[8]+","+row[9]+","+row[10]+","+
                 row[11]+","+row[12]+","+row[13]+","+row[14]+","+row[15]+","+row[16]+","+row[17]+",'"+row[18]+"',"+row[19]+","+row[20]+","+
                 row[21]+","+row[22]+","+row[23]+","+row[24]+","+row[25]+","+row[26]+",'"+row[27]+"','"+row[28]+"','','','','','','','','','','',"+ 0+","+0+","+
                 0+","+0+","+0+","+0+","+0+","+0+","+0+","+0+","+0+","+0+","+
                 0+","+0+","+0+","+0+","+0+","+0+","+0+","+0+","+0+","+0+","+
                 0+","+0+","+0+","+0+","+0+","+0+","+0+",'motech-stepsovc','motech-stepsovc',GETDATE(),GETDATE()"+" );" ;
 
     }
 
 
 
 }
