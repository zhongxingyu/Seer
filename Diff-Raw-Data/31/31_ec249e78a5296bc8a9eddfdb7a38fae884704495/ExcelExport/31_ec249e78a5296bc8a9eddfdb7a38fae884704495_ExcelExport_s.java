 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package netcracker.service;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import netcracker.dao.DAOFactory;
 import org.apache.poi.hssf.usermodel.*;
 import org.apache.poi.hssf.util.HSSFColor;
 import org.apache.poi.ss.usermodel.*;
 import org.apache.poi.util.IOUtils;
 
 /**
  *
  * @author lastride
  */
 public class ExcelExport {
     /**
      * Exports database to .xls
      * @return 
      */
    public static boolean exportToExcel() {
         PreparedStatement stmt = null;
         ResultSet result = null;
         Connection conn = DAOFactory.createConnection();
         FileOutputStream fos = null;
         HSSFWorkbook workbook = new HSSFWorkbook();
         try {
             String[] tableNames = {"adverts", "advertsForStudents", "employees", "faculties",
                 "interests", "interestsForStudents", "intervals",
                 "intervalsForStudents", "intervalStatuses",
                 "messages", "results", "roles", "skills",
                 "skillsForStudents", "skillsTypes", "students",
                 "universities"};
             
             ArrayList<HSSFSheet> sheets = new ArrayList<HSSFSheet>();
             for (int i = 0; i < tableNames.length; i++) {
                 sheets.add(workbook.createSheet(tableNames[i]));
             }
             
             String[] advertsFields = {"id_advert", "advert_name"};
             String[] advertsForStudentsFields = {"id_student", "id_advert", "notes"};
             String[] employeesFields = {"id_employee", "login", "password",
                 "first_name", "last_name", "email", "id_role"};
             String[] facultiesFields = {"id_faculty", "id_university", "faculty_name"};
             String[] interestsFields = {"id_interest", "interest_name"};
             String[] interestsForStudentsFields = {"id_interest", "id_student", "mark", "notes"};
             String[] intervalsFields = {"id_interval", "start_time", "end_time", "interviewers_count", "id_interval_status"};
             String[] intervalsForStudentsFields = {"id_interval", "id_student"};
             String[] intervalStatusesFields = {"id_interval_status", "interval_status_name"};
             String[] messagesFields = {"id_student", "edit_values", "status"};
             String[] resultsFields = {"id_student", "id_employee", "comment"};
             String[] rolesFields = {"id_role", "role_name"};
             String[] skillsFields = {"id_skill", "skill_name", "id_skill_type"};
             String[] skillsForStudentsFields = {"id_skill", "id_student", "mark", "notes"};
             String[] skillsTypesFields = {"id_skill_type", "skill_type_name"};
             String[] studentsFields = {"id_student", "first_name", "last_name", "middle_name", "course", "study_end_year",
                 "id_faculty", "email1", "email2", "phone1", "extra_contacts", "why", "experience",
                 "extra", "reg_day", "photo"};
             String[] universitiesFields = {"id_university", "university_name"};
             
             ArrayList<String[]> strings = new ArrayList<String[]>();
             strings.add(advertsFields);
             strings.add(advertsForStudentsFields);
             strings.add(employeesFields);
             strings.add(facultiesFields);
             strings.add(interestsFields);
             strings.add(interestsForStudentsFields);
             strings.add(intervalsFields);
             strings.add(intervalsForStudentsFields);
             strings.add(intervalStatusesFields);
             strings.add(messagesFields);
             strings.add(resultsFields);
             strings.add(rolesFields);
             strings.add(skillsFields);
             strings.add(skillsForStudentsFields);
             strings.add(skillsTypesFields);
             strings.add(studentsFields);
             strings.add(universitiesFields);
             
             
             HSSFCellStyle style = workbook.createCellStyle();
             style.setBorderTop(CellStyle.BORDER_THIN); 
             style.setBorderLeft(CellStyle.BORDER_THIN);
             style.setBorderRight(CellStyle.BORDER_THIN);
             style.setBorderBottom(CellStyle.BORDER_THIN); 
             style.setAlignment(CellStyle.ALIGN_CENTER);
             style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
             
             HSSFFont font = workbook.createFont();
             font.setFontName(HSSFFont.FONT_ARIAL);
             font.setFontHeightInPoints((short) 12);
             font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
             font.setColor(HSSFColor.BROWN.index);
             
             style.setFont(font);
             
             HSSFRow row = null;
             for (int i = 0; i < sheets.size(); i++) {
                 row = sheets.get(i).createRow(0);
                 row.setHeightInPoints(20f);
                 for (int j = 0; j < strings.get(i).length; j++) {
                     HSSFCell cell = row.createCell(j);
                     cell.setCellValue(strings.get(i)[j]);
                     cell.setCellStyle(style);
                     sheets.get(i).autoSizeColumn((short) j);
                 }
             }
             
             StringBuffer sbEmployees = null;
             HSSFRow rowT = null;
             for (int i = 0; i < tableNames.length; i++) {
                 sbEmployees = new StringBuffer();
                 sbEmployees.append("SELECT * FROM ");
                 sbEmployees.append(tableNames[i]);
                 stmt = conn.prepareStatement(sbEmployees.toString());
                 result = stmt.executeQuery();
                 int c = 0;
                 while (result.next()) {
                     c++;
                     rowT = sheets.get(i).createRow(c);
                     for (int j = 0; j < strings.get(i).length; j++) {
                         HSSFCell cellT = rowT.createCell(j);
                         if (sheets.get(i).getSheetName().equals("students")) {
                             HSSFCellStyle styleT = workbook.createCellStyle();
                             styleT.setAlignment(CellStyle.ALIGN_CENTER);
                             styleT.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
                             cellT.setCellStyle(styleT);
                         }
                         if (strings.get(i)[j].equals("photo")) {
                             InputStream img = result.getBinaryStream("photo");
                                 rowT.setHeightInPoints(70f);
                                 byte[] bytes = IOUtils.toByteArray(img);
                                 int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
                                 img.close();
                                 CreationHelper helper = workbook.getCreationHelper();
                                 Drawing drawing = sheets.get(i).createDrawingPatriarch();
                                 ClientAnchor anchor = helper.createClientAnchor();
                                 anchor.setAnchorType(ClientAnchor.MOVE_DONT_RESIZE);
                                 anchor.setCol1(j);
                                 anchor.setRow1(c);
                                 sheets.get(i).autoSizeColumn(j);
                                 Picture pict = drawing.createPicture(anchor, pictureIdx);
                                 pict.getPreferredSize();
                                 pict.resize(0.1);
                         } else {
                             cellT.setCellValue(result.getString(strings.get(i)[j]));
                         }
                         sheets.get(i).autoSizeColumn((short) j);
                     }
                 }
             }
         } catch (IOException ex) {
             Logger.getLogger(ExcelExport.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(ExcelExport.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closePreparedStatement(stmt);
         }
         /**
          * Write xls file
          */
         try {
            fos = new FileOutputStream(new File("/home/lastride/DataBase.xls"));
             workbook.write(fos);
         } catch (IOException ex) {
             Logger.getLogger(ExcelExport.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             if (fos != null) {
                 try {
                     fos.flush();
                     fos.close();
                 } catch (IOException ex) {
                     Logger.getLogger(ExcelExport.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
         
        return true;
     }
 }
