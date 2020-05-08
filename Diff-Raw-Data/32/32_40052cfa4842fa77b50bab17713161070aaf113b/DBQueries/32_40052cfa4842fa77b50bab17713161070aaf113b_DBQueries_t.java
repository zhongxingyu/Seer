 package yazamimDB;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import yazamimDB.Binding.MarketingStatus;
 import yazamimDB.helpers.InstructorTypes;
 
 public class DBQueries {
 
 	private static final String GET_EMPLOYEES_LIST = "SELECT e.username, e.name, e.phone, e.email, e.password, "
 			+ "e.areaNum, u.rolename, a.areaName FROM employees e INNER JOIN userroles u ON e.username=u.username "
 			+ "LEFT JOIN areas a ON a.areaNum=e.areaNum";
 
 	private static final String GET_EMPLOYEE_DETAILS = "SELECT username, name, phone, email, password, e.areaNum, a.areaName "
 			+ "FROM employees e inner join areas a on e.areaNum = a.areaNum WHERE username=?";
 
 	private static final String DELETE_EMPLOYEE = "DELETE FROM employees WHERE username=?";
 
 	private static final String DELETE_EMPLOYEE_ROLE = "DELETE FROM userroles WHERE username=?";
 
 	private static final String INSERT_EMPLOYEE = "INSERT INTO employees (username, name, phone, email, password, "
 			+ "areaNum) VALUES(?,?,?,?,?,?)";
 
 	private static final String UPDATE_EMPLOYEE = "UPDATE employees SET name=?, phone=?, email=?, password=?, areaNum=? WHERE username=? ";
 
 	private static final String REPLACE_EMPLOYEE_ROLE = "REPLACE userroles (username, rolename) VALUES(?,?)";
 
 	private static final String GET_SCHOOLS_LIST = "SELECT s.schoolNum, s.schoolName, c.areaNum, a.areaName, "
 			+ "s.cityNum, c.cityName, s.address, s.principleName, s.phone, s.fax, s.email, s.contactName, "
 			+ "s.contactPhone, s.contactMail, s.netId, s.typeId, s.comments FROM schools s INNER JOIN cities c ON "
 			+ "s.cityNum=c.cityNum INNER JOIN areas a ON a.areaNum=c.areaNum ";
 
 	private static final String GET_SCHOOLS_WITH_MARKETING_STATUS = "SELECT s.schoolNum, s.schoolName, "
 			+ "c.areaNum, a.areaName, s.cityNum, c.cityName, s.address, s.principleName, s.phone, s.fax, s.email, "
 			+ "s.contactName, s.contactPhone, s.contactMail, s.netId, s.typeId, s.comments, b.bindingYear, "
 			+ "b.marketingStatus FROM schools s INNER JOIN cities c ON s.cityNum=c.cityNum INNER JOIN areas a "
 			+ "ON a.areaNum=c.areaNum LEFT JOIN bindings b ON s.schoolNum=b.schoolNum AND bindingYear=?";
 
 	private static final String REPLACE_EMPTY_BINDING = "REPLACE bindings (schoolNum, bindingYear, marketingStatus) "
 			+ "VALUES(?,?,?)";
 
 	private static final String UPDATE_MARKETING_STATUS = "UPDATE bindings SET marketingStatus=? "
 			+ "WHERE schoolNum=? AND bindingYear=?";
 
 	private static final String GET_SCHOOL_BY_NUM = GET_SCHOOLS_LIST
 			+ " WHERE s.schoolNum=?";
 
 	private static final String GET_SCHOOLS_IN_AREA = GET_SCHOOLS_LIST
 			+ " WHERE a.areaNum=?";
 
 	private static final String GET_SCHOOLS_BY_NETWORK = GET_SCHOOLS_LIST
 			+ " WHERE netId=?";
 
 	private static final String GET_SCHOOLS_BY_TYPE = GET_SCHOOLS_LIST
 			+ " WHERE typeId=?";
 
 	private static final String GET_SCHOOLS_IN_CITY = GET_SCHOOLS_LIST
 			+ " WHERE c.cityNum=?";
 
 	private static final String UPDATE_SCHOOL = "UPDATE schools SET schoolName=?, address=?, principleName=?, "
 			+ "phone=?, fax=?, email=?, contactName=?, contactPhone=?, contactMail=?, cityNum=?, netId=?, typeId=?, "
 			+ "comments=? WHERE schoolNum=?";
 
 	private static final String INSERT_SCHOOL = "INSERT schools (schoolName, address, principleName, "
 			+ "phone, fax, email, contactName, contactPhone, contactMail, cityNum, netId, typeId, comments) "
 			+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 
 	private static final String DELETE_SCHOOL = "DELETE FROM schools WHERE schoolNum=? ";
 
 	private static final String GET_CITY_NAME = "SELECT cityName FROM cities ORDER BY cityName";
 
 	private static final String GET_INSTRUCTORS_LIST = "SELECT * from( select f.*, 't' as insType, NULL as companyNum, NULL as companyName from instructors f  where instructorNum not in (select studentNum from students) "
 			+ "and instructorNum not in (select businessInsNum from businessinstructors) union select f.*, 's' as insType, s.institutionNum as companyNum, i.institutionName as companyName from instructors f inner join students s on f.instructorNum = s.studentNum "
 			+ "inner join institutions i on s.institutionNum = i.institutionNum union select f.*, 'b' as insType, c.companyNum, c.companyName from instructors f inner join businessinstructors bi on f.instructorNum = bi.businessInsNum left join companies c on bi.companyNum = c.companyNum) a order by instructorNum";
 
 	private static final String GET_ACADEMIC_INSTITUTIONS_LIST = "SELECT institutionNum, institutionName "
 			+ "FROM institutions ORDER BY institutionName";
 
 	private static final String GET_CITIES_LIST = "SELECT cityNum, cityName, a.areaNum, a.areaName FROM cities c inner join areas a on c.areaNum = a.areaNum";
 
 	private static final String GET_COMPANIES_LIST = "SELECT companyNum, companyName FROM companies ORDER BY companyName";
 
 	private static final String GET_GROUP_TYPES_LIST = "SELECT groupTypeNum, groupTypeName, contactName, contactPhone, contactMail FROM grouptypes";
 
 	private static final String UPDATE_GROUP_TYPE = "UPDATE grouptypes SET contactName=?, contactPhone=?, contactMail=? WHERE groupTypeNum=? ";
 
 	private static final String GET_NETWORKS_LIST = "SELECT netId, netName FROM schoolnetwork ORDER BY netName";
 
 	private static final String GET_SCHOOL_TYPES_LIST = "SELECT typeId, typeName FROM schooltype ORDER BY typeId";
 
 	private static final String GET_PROGRAMS_LIST = "SELECT programNum, programName FROM programs";
 
 	private static final String GET_PROGRAM_BY_GROUP = "SELECT p.programNum, programName FROM programs p INNER JOIN groups g ON "
 			+ "p.programNum = g.programNum WHERE g.schoolNum = ? and g.groupNum = ?";
 
 	private static final String GET_STEPS_LIST = "SELECT stepNum, stepName FROM steps";
 
 	private static final String GET_GROUPS_LIST_BY_YEAR = "SELECT s.schoolNum, s.schoolName, g.groupNum, g.groupName, "
 			+ "gt.groupTypeNum, gt.groupTypeName, a.areaNum, a.areaName, c.cityNum, c.cityName, g.meetingDay, g.meetingTime, st.stepNum, "
 			+ "st.stepName, p.programNum, p.programName, g.activityYear FROM schools s INNER JOIN groups g ON "
 			+ "s.schoolNum = g.schoolNum INNER JOIN grouptypes gt ON g.groupTypeNum = gt.groupTypeNum INNER JOIN "
 			+ "cities c ON s.cityNum = c.cityNum INNER JOIN steps st ON g.stepNum = st.stepnum INNER JOIN programs p "
 			+ "ON g.programNum = p.programNum INNER JOIN areas a ON c.areaNum = a.areaNum WHERE g.activityYear = ?";
 
 	private static final String GET_GROUPS_LIST = "SELECT s.schoolNum, s.schoolName, g.groupNum, g.groupName, "
 			+ "gt.groupTypeNum, gt.groupTypeName, a.areaNum, a.areaName, c.cityNum, c.cityName, g.meetingDay, g.meetingTime, st.stepNum, "
 			+ "st.stepName, p.programNum, p.programName, g.activityYear FROM schools s INNER JOIN groups g ON "
 			+ "s.schoolNum = g.schoolNum INNER JOIN grouptypes gt ON g.groupTypeNum = gt.groupTypeNum INNER JOIN "
 			+ "cities c ON s.cityNum = c.cityNum INNER JOIN steps st ON g.stepNum = st.stepnum INNER JOIN programs p "
 			+ "ON g.programNum = p.programNum INNER JOIN areas a ON c.areaNum = a.areaNum ";
 
 	private static final String GET_SCHOOL_GROUPS_LIST = "SELECT s.schoolNum, s.schoolName, g.groupNum, g.groupName, "
 			+ "gt.groupTypeNum, gt.groupTypeName, c.cityNum, c.cityName, g.meetingDay, g.meetingTime, st.stepNum, "
 			+ "st.stepName, p.programNum, p.programName, g.activityYear FROM schools s INNER JOIN groups g ON "
 			+ "s.schoolNum = g.schoolNum INNER JOIN grouptypes gt ON g.groupTypeNum = gt.groupTypeNum INNER JOIN "
 			+ "cities c ON s.cityNum = c.cityNum INNER JOIN steps st ON g.stepNum = st.stepnum INNER JOIN programs p "
 			+ "ON g.programNum = p.programNum WHERE s.schoolNum = ? ORDER BY g.groupName";
 
 	private static final String GET_GROUP_BY_NUM = "SELECT s.schoolNum, s.schoolName, g.groupNum, g.groupName, "
 			+ "gt.groupTypeNum, gt.groupTypeName, c.cityNum, c.cityName, g.meetingDay, g.meetingTime, st.stepNum, "
 			+ "st.stepName, p.programNum, p.programName, g.activityYear FROM schools s INNER JOIN groups g ON "
 			+ "s.schoolNum = g.schoolNum INNER JOIN grouptypes gt ON g.groupTypeNum = gt.groupTypeNum INNER JOIN "
 			+ "cities c ON s.cityNum = c.cityNum INNER JOIN steps st ON g.stepNum = st.stepnum INNER JOIN programs p "
 			+ "ON g.programNum = p.programNum WHERE g.schoolNum=? and g.groupNum=?";
 
 	private static final String GET_GROUP_MEMBERS_LIST = "SELECT gm.memberId, gm.memberName, gm.memberPhone, "
 			+ "gm.memberEmail FROM groupmembers gm WHERE gm.schoolNum=? and gm.groupNum=?";
 
 	private static final String DELETE_GROUP_MEMBER = "DELETE FROM groupmembers "
 			+ "WHERE schoolNum=? and groupNum=? and memberId=?";
 
 	private static final String DELETE_GROUP_MEMBERS = "DELETE FROM groupmembers "
 			+ "WHERE schoolNum=? and groupNum=? ";
 
 	private static final String REPLACE_GROUP_MEMBER = "REPLACE groupmembers (schoolNum, groupNum, memberId, "
 			+ "memberName, memberPhone, memberEmail) VALUES(?,?,?,?,?,?)";
 
 	private static final String GET_GROUP_BANK_DETAILS = "SELECT openAccountDate, branchNum, branchName, "
 			+ "accountNum FROM groups WHERE schoolNum=? AND groupNum=?";
 
 	private static final String UPDATE_BANK_DETAILS = "UPDATE groups SET openAccountDate=?, branchNum=?, branchName=?, "
 			+ "accountNum=? WHERE schoolNum=? AND groupNum=?";
 
 	private static final String GET_AREAS_LIST = "SELECT areaNum, areaName FROM areas";
 
 	private static final String GET_AREA_BY_NUM = "SELECT areaName FROM areas WHERE areaNum=? ";
 
 	private static final String GET_MANUFACTURERS_LIST = "SELECT manuNum, name, address, contactName, contactPhone, contactMail "
 			+ "FROM manufacturers ";
 
 	private static final String GET_MANUFACTURER_BY_NUM = GET_MANUFACTURERS_LIST
 			+ " WHERE manuNum =? ";
 
 	private static final String GET_PRODUCT_BY_GROUP = "SELECT p.productNum, p.groupNum, groupName, p.schoolNum, productName, sentProductForm, productFormSentDate, gotProductForm, productFormGotDate, "
 			+ "description, imageLink, manuNum, catId FROM products p inner join groups g on p.schoolNum = g.schoolNum and p.groupNum = g.groupNum WHERE p.schoolNum = ? and p.groupNum = ? ";
 
 	private static final String GET_PRODUCT_BY_CATEGORY = "SELECT p.productNum, p.groupNum, groupName, p.schoolNum, productName, sentProductForm, productFormSentDate, gotProductForm, productFormGotDate, "
 			+ "description, imageLink, manuNum, catId FROM products p inner join groups g on p.schoolNum = g.schoolNum and p.groupNum = g.groupNum WHERE p.catId=?";
 
 	private static final String INSERT_PRODUCT = "INSERT INTO products (schoolNum, groupNum, productNum, productName, description, imageLink, manuNum, sentProductForm, productFormSentDate, gotProductForm, productFormGotDate, catId) VALUES (?,?,?,?,?,?,?,?,?,?,?,?) ";
 
 	private static final String GET_PRODUCT_CATEGORIES_LIST = "SELECT catId, catName FROM product_categories ";
 
 	private static final String GET_PRODUCT_CATEGORY_BY_NUM = GET_PRODUCT_CATEGORIES_LIST
 			+ " WHERE catId = ?";
 
 	private static final String UPDATE_PRODUCT_DETAILS = "UPDATE products SET productName =?, description =?, imageLink =?, manuNum =?, sentProductForm=?, productFormSentDate=?, gotProductForm=?, productFormGotDate=?, catId=? "
 			+ "WHERE schoolNum =? and groupNum =? ";
 
 	private static final String DELETE_PRODUCT = "DELETE FROM products WHERE schoolNum =? and groupNum =?";
 
 	private static final String GET_SPONSORS_LIST = "SELECT sponsorNum, sponsorName, contactName, contactPhone, contactMail, description FROM sponsors ";
 
 	private static final String GET_SPONSOR_BY_NUM = GET_SPONSORS_LIST
 			+ "WHERE sponsorNum = ?";
 
 	private static final String GET_SPONSOR_PAYMENTS = "SELECT paymentNum, b.bindingYear, sc.schoolNum, g.groupNum, obligationDate, receiveDate, amount, p.comments "
 			+ "FROM sponsors s inner join payments p on s.sponsorNum = p.sponsorNum inner join bindings b on p.bindingYear = b.bindingYear "
 			+ "and p.schoolNum = b.schoolNum inner join schools sc on b.schoolNum = sc.schoolNum inner join groups g on sc.schoolNum = g.schoolNum and p.bindingYear = g.activityYear "
 			+ "WHERE s.sponsorNum = ? GROUP BY p.paymentNum";
 
 	private static final String INSERT_SPONSOR = "INSERT INTO sponsors (sponsorName, contactName, contactPhone, contactMail, description) VALUES (?,?,?,?,?) ";
 
 	private static final String UPDATE_SPONSOR = "UPDATE sponsors SET sponsorName=?, contactName=?, contactPhone=?, contactMail=?, description=? WHERE sponsorNum=? ";
 
 	private static final String GET_GROUP_PAYMENTS = "SELECT s.sponsorNum, paymentNum, b.bindingYear, sc.schoolNum, g.groupNum, obligationDate, receiveDate, amount, "
 			+ "paymentContact, p.phone, p.address, comments "
 			+ "FROM sponsors s inner join payments p on s.sponsorNum = p.sponsorNum inner join bindings b on p.bindingYear = b.bindingYear "
 			+ "and p.schoolNum = b.schoolNum inner join schools sc on b.schoolNum = sc.schoolNum inner join groups g on sc.schoolNUm = g.schoolNum "
 			+ "WHERE g.schoolNum = ? and p.bindingYear = ? "
 			+ "GROUP BY p.paymentNum ";
 
 	private static final String DELETE_GROUP_PAYMENT = "DELETE FROM payments WHERE schoolNum =? and bindingYear =? and paymentNum =? ";
 
 	private static final String DELETE_GROUP_PAYMENTS = "DELETE FROM payments WHERE schoolNum =? and bindingYear =? ";
 
 	private static final String INSERT_GROUP_PAYMENT = "INSERT INTO payments (paymentNum, schoolNum, bindingYear, sponsorNum, receiveDate, obligationDate, amount, paymentContact, "
 			+ "phone, address, comments) VALUES(?,?,?,?,?,?,?,?,?,?,?) ";
 
 	private static final String UPDATE_GROUP_PAYMENT = "UPDATE payments set sponsorNum=?, receiveDate=?, obligationDate=?, amount=?, paymentContact=?, phone=?, address=?, comments=? WHERE paymentNum=? and schoolNum=? and bindingYear=?";
 
 	private static final String GET_INSTRUCTOR_BY_NUM = "SELECT * FROM instructors i LEFT JOIN students s ON "
 			+ "i.instructorNum = s.studentNum LEFT JOIN businessinstructors b ON i.instructorNum = b.businessInsNum "
 			+ "WHERE instructorNum = ?";
 
 	private static final String GET_INSTRUCTOR_GROUPS_LIST = "SELECT s.schoolNum, s.schoolName, g.groupNum, "
 			+ "g.groupName, gt.groupTypeNum, gt.groupTypeName, c.cityNum, c.cityName, g.meetingDay, g.meetingTime, "
 			+ "st.stepNum, st.stepName, p.programNum, p.programName, g.activityYear FROM schools s INNER JOIN "
 			+ "groups g ON s.schoolNum = g.schoolNum INNER JOIN grouptypes gt ON g.groupTypeNum = gt.groupTypeNum "
 			+ "INNER JOIN cities c ON s.cityNum = c.cityNum INNER JOIN steps st ON g.stepNum = st.stepnum INNER JOIN "
 			+ "programs p ON g.programNum = p.programNum INNER JOIN groupinstructors gi ON s.schoolNum=gi.schoolNum "
 			+ "AND g.groupNum = gi.groupNum AND gi.instructorNum=? ORDER BY g.activityYear DESC";
 
 	private static final String DELETE_BUISNESS_INSTRUCTOR = "DELETE FROM businessinstructors WHERE businessInsNum=?";
 
 	private static final String DELETE_STUDENT_INSTRUCTOR = "DELETE FROM students WHERE studentNum=?";
 
 	private static final String UPDATE_INSTRUCTOR = "UPDATE instructors SET instructorName=?, address=?, phone=?, "
 			+ "email=?, occupation=?, birthdate=?, education=?, instructingExp=?, professionalExp=?, hobbies=?, "
 			+ "howCame=?, volunteerExp=?, expectations=?, skills=?, impressions=? WHERE instructorNum=?";
 
 	private static final String REPLACE_BUSINESS_INSTRUCTOR = "REPLACE businessinstructors (businessInsNum, "
 			+ "companyNum) VALUES(?,?)";
 
 	private static final String REPLACE_STUDENT_INSTRUCTOR = "REPLACE students (studentNum, institutionNum, "
 			+ "academicYear) VALUES(?,?,?)";
 
 	private static final String INSERT_INSTRUCTOR = "INSERT instructors (instructorName, address, phone,  email, "
 			+ "occupation, birthdate, education, instructingExp, professionalExp, hobbies, howCame, volunteerExp, "
 			+ "expectations, skills, impressions) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 
 	private static final String INSERT_MANUFACTURER = "INSERT INTO manufacturers (name, address, contactName, contactPhone, contactMail) VALUES(?,?,?,?,?) ";
 
 	private static final String UPDATE_MANUFACTURER = "UPDATE manufacturers SET name=?, address=?, contactName=?, contactPhone=?, contactMail=? WHERE manuNum=? ";
 
 	private static final String GET_MANUFACTURER_PRODUCTS = "SELECT p.productNum, p.groupNum, g.groupName, g.activityYear, g.programNum, pr.programName, s.schoolNum, s.schoolName, p.productName, p.catId "
 			+ "FROM products p inner join groups g on p.schoolNum = g.schoolNum and p.groupNum = g.groupNum inner join schools s on g.schoolNum = s.schoolNum inner join programs pr on g.programNum = pr.programNum "
 			+ "WHERE manuNum=? ";
 
 	private static final String GET_BINDING_BY_SCHOOL = "SELECT b.schoolNum, b.bindingYear, b.gotContract, b.gotContractDate, b.gotRegistration, b.registrationDate, b.registrationDate, b.sentMaterials, b.materialsSentDate, b.isGotMaterials, b.materialsGotDate, b.gotMaterialsContact, b.materialsGotPhone  "
 			+ "FROM bindings b inner join schools s on b.schoolNum = s.schoolNum inner join groups g on s.schoolNum = g.schoolNum and b.bindingYear = g.activityYear "
 			+ "WHERE g.schoolNum =? and g.activityYear=?";
 
 	private static final String INSERT_NEW_GROUP_BINDING = "INSERT bindings (schoolNum, bindingYear, marketingStatus) VALUES(?,?,'S')";
 
 	private static final String UPDATE_NEW_GROUP_BINDING = "UPDATE bindings SET marketingStatus='S' WHERE schoolNum=? and bindingYear=? ";
 
 	private static final String UPDATE_BINDING = "UPDATE bindings SET gotContract=?, registrationDate=?, sentMaterials=?, materialsSentDate=?, isGotMaterials=?, materialsGotDate=?, gotMaterialsContact=?, materialsGotPhone=?, gotContractDate=?, gotRegistration=? WHERE schoolNum=? and bindingYear=? ";
 
 	private static final String INSERT_GROUP = "INSERT groups (groupName, activityYear, meetingDay, meetingTime, schoolNum, stepNum, groupTypeNum, programNum) VALUES (?,?,?,?,?,?,?,?)";
 
 	private static final String UPDATE_GROUP = "UPDATE groups SET groupName=?, activityYear=?, meetingDay=?, meetingTime=?, stepNum=?, groupTypeNum=?, programNum=? WHERE schoolNum=? and groupNum=? ";
 
 	private static final String DELETE_GROUP_INSTRUCTORS = "DELETE FROM groupinstructors WHERE schoolNum=? and groupNum=? ";
 
 	private static final String DELETE_GROUP = "DELETE FROM groups WHERE schoolNum=? and groupNum=?";
 
 	private static final String DELETE_BINDING = "DELETE FROM bindings WHERE schoolNum=? and bindingYear=? ";
 
 	private static final String CHECK_SCHOOL_AND_ACTIVITY_IN_GROUPS = "SELECT groupNum FROM groups WHERE schoolNum=? and activityYear=? ";
 
 	private static final String GET_GROUP_INSTRUCTORS_LIST = "SELECT * from( select f.*, 't' as insType from instructors f  where instructorNum not in (select studentNum from students) "
 			+ "and instructorNum not in (select businessInsNum from businessinstructors) union select f.*, 's' as insType from instructors f inner join students s on f.instructorNum = s.studentNum "
 			+ "union select f.*, 'b' as insType from instructors f inner join businessinstructors bi on f.instructorNum = bi.businessInsNum) a inner join groupinstructors gi on a.instructorNum = gi.instructorNum where gi.schoolNum=? and gi.groupNum=? order by a.instructorNum ";
 
 	private static final String GET_INSTRUCTOR_TYPE = "SELECT insType from( select f.*, 't' as insType from instructors f  where instructorNum not in (select studentNum from students) "
 			+ "and instructorNum not in (select businessInsNum from businessinstructors) union select f.*, 's' as insType from instructors f inner join students s on f.instructorNum = s.studentNum "
 			+ "union select f.*, 'b' as insType from instructors f inner join businessinstructors bi on f.instructorNum = bi.businessInsNum) a "
 			+ "WHERE instructorNum = ?";
 
 	private static final String INSERT_GROUP_INSTRUCTOR = "REPLACE groupinstructors (groupNum, schoolNum, instructorNum) VALUES(?,?,?)";
 
 	private static final String DELETE_GROUP_INSTRUCTOR = "DELETE FROM groupinstructors where groupNum=? and schoolNum=? and instructorNum=? ";
 
 	private static final String GET_COMPANY_BY_NUM = "SELECT companyNum, companyName FROM companies WHERE companyNum=?";
 
 	private static final String GET_EMPLOYEE_AREA = "SELECT areaNum FROM employees WHERE username=? ";
 
 	private static final String GET_EMPLOYEE_ROLENAME = "SELECT rolename FROM userroles WHERE username=? ";
 
 	private static final String INSERT_NEW_AREA = "INSERT INTO areas (areaName) VALUES(?)";
 
 	private static final String UPDATE_AREA_NAME = "UPDATE areas SET areaName=? WHERE areaNum=? ";
 
 	private static final String DELETE_AREA = "DELETE FROM areas WHERE areaNum=? ";
 
 	private static final String INSERT_NEW_CITY = "INSERT INTO cities (cityName, areaNum) VALUES(?,?)";
 
 	private static final String UPDATE_CITY_NAME = "UPDATE cities SET cityName=? WHERE cityNum=? ";
 
 	private static final String DELETE_CITY = "DELETE FROM cities WHERE cityNum=? ";
 
 	private static final String INSERT_NEW_SCHOOL_NETWORK = "INSERT INTO schoolnetwork (netName) VALUES(?)";
 
 	private static final String UPDATE_SCHOOL_NETWORK_NAME = "UPDATE schoolnetwork SET netName=? WHERE netId=? ";
 
 	private static final String DELETE_SCHOOL_NETWORK = "DELETE FROM schoolnetwork WHERE netId=? ";
 
 	private static final String INSERT_NEW_SCHOOL_TYPE = "INSERT INTO schooltype (typeName) VALUES(?)";
 
 	private static final String UPDATE_SCHOOL_TYPE_NAME = "UPDATE schooltype SET typeName=? WHERE typeId=? ";
 
 	private static final String DELETE_SCHOOL_TYPE = "DELETE FROM schooltype WHERE typeId=? ";
 
 	private static final String INSERT_NEW_GROUP_TYPE = "INSERT INTO grouptypes (groupTypeName) VALUES(?)";
 
 	private static final String UPDATE_GROUP_TYPE_NAME = "UPDATE grouptypes SET groupTypeName=? WHERE groupTypeNum=? ";
 
 	private static final String DELETE_GROUP_TYPE = "DELETE FROM grouptypes WHERE groupTypeNum=? ";
 
 	private static final String INSERT_NEW_PROGRAM = "INSERT INTO programs (programName) VALUES(?)";
 
 	private static final String UPDATE_PROGRAM_NAME = "UPDATE programs SET programName=? WHERE programNum=? ";
 
 	private static final String DELETE_PROGRAM = "DELETE FROM programs WHERE programNum=? ";
 
 	private static final String INSERT_NEW_STEP = "INSERT INTO steps (stepName) VALUES(?)";
 
 	private static final String UPDATE_STEP_NAME = "UPDATE steps SET stepName=? WHERE stepNum=? ";
 
 	private static final String DELETE_STEP = "DELETE FROM steps WHERE stepNum=? ";
 
 	private static final String INSERT_NEW_PRODUCT_CATEGORY = "INSERT INTO product_categories (catName) VALUES(?)";
 
 	private static final String UPDATE_PRODUCT_CATEGORY_NAME = "UPDATE product_categories SET catName=? WHERE catId=? ";
 
 	private static final String DELETE_PRODUCT_CATEGORY = "DELETE FROM product_categories WHERE catId=? ";
 
 	private static final String INSERT_NEW_COMPANY = "INSERT INTO companies (companyName) VALUES(?)";
 
 	private static final String UPDATE_COMPANY_NAME = "UPDATE companies SET companyName=? WHERE companyNum=? ";
 
 	private static final String DELETE_COMPANY = "DELETE FROM companies WHERE companyNum=? ";
 
 	private static final String INSERT_NEW_INSTITUTION = "INSERT INTO institutions (institutionName) VALUES(?)";
 
 	private static final String UPDATE_INSTITUTION_NAME = "UPDATE institutions SET institutionName=? WHERE institutionNum=? ";
 
 	private static final String DELETE_INSTITUTION = "DELETE FROM institutions WHERE institutionNum=? ";
 
 	private static final String INSERT_UPDATE = "INSERT INTO updates(up_date, description, link, username) VALUES(?,?,?,?) ";
 
 	private static final String GET_UPDATES_LIST = "SELECT updateNum, up_date, description, link, u.username, name, e.areaNum, a.areaName FROM"
 			+ " updates u inner join employees e on u.username = e.username inner join areas a on e.areaNum = a.areaNum ";
 
 	private static final String INSERT_UPDATE_REMOVED = "INSERT INTO updates_removed (updateNum, username) VALUES (?,?) ";
 
 	private static final String GET_GROUP_STATUSES_LIST = "SELECT statusNum, statusDate, details, schoolNum, groupNum FROM groupstatuses WHERE schoolNum=? and groupNum=? ";
 
 	private static final String INSERT_GROUP_STATUS = "INSERT INTO groupstatuses (statusNum, statusDate, details, groupNum, schoolNum) VALUES (?,?,?,?,?) ";
 
 	private static final String DELETE_GROUP_STATUS = "DELETE FROM groupstatuses WHERE schoolNum=? and groupNum=? and statusNum=? ";
 
 	private static final String DELETE_GROUP_STATUSES = "DELETE FROM groupstatuses WHERE schoolNum=? and groupNum=? ";
 
 	private static final String REPORT_SCHOOLS_BY_MARKETING_STATUS = "SELECT CASE "
 			+ "WHEN b.marketingStatus = 'L' THEN 'סיכויים נמוכים' "
 			+ "WHEN b.marketingStatus = 'M' THEN 'עדיין לא ברור' "
 			+ "WHEN b.marketingStatus = 'H' THEN 'סיכויים גבוהים' "
 			+ "WHEN b.marketingStatus = 'S' THEN 'התחיל פעילות' "
 			+ "ELSE 'לא שווק' END AS marketingStatus, "
 			+ "b.bindingYear, typeName, netName, s.address,s.cityNum, c.cityName,c.areaNum, a.areaName, s.schoolNum, s.schoolName "
 			+ "FROM schools s INNER JOIN cities c ON s.cityNum=c.cityNum INNER JOIN areas a ON a.areaNum=c.areaNum "
 			+ "INNER JOIN schoolnetwork sn ON sn.netId = s.netId INNER JOIN schooltype st ON st.typeId = s.typeId "
 			+ "LEFT JOIN bindings b ON s.schoolNum=b.schoolNum and bindingYear=";
 
 	private static final String REPORT_SCHOOLS_BY_NET = "SELECT typeName, s.netId, netName, s.contactMail, "
 			+ "s.contactPhone, s.contactName, s.email, s.fax, s.phone, s.principleName, s.address,s.cityNum, "
 			+ "c.cityName,c.areaNum, a.areaName, s.schoolNum, s.schoolName "
 			+ "FROM schools s INNER JOIN cities c ON s.cityNum=c.cityNum INNER JOIN areas a "
 			+ "ON a.areaNum=c.areaNum INNER JOIN schoolnetwork sn ON "
 			+ "sn.netId = s.netId INNER JOIN schooltype st ON st.typeId = s.typeId";
 
 	private static final String REPORT_SCHOOLS_BY_TYPE = "SELECT st.typeId, typeName, s.netId, netName, s.contactMail, "
 			+ "s.contactPhone, s.contactName, s.email, s.fax, s.phone, s.principleName, s.address,s.cityNum, "
 			+ "c.cityName,c.areaNum, a.areaName, s.schoolNum, s.schoolName "
 			+ "FROM schools s INNER JOIN cities c ON s.cityNum=c.cityNum INNER JOIN areas a "
 			+ "ON a.areaNum=c.areaNum INNER JOIN schoolnetwork sn ON "
 			+ "sn.netId = s.netId INNER JOIN schooltype st ON st.typeId = s.typeId";
 
 	private static final String REPORT_GROUPS_LIST_BY_YEAR = "SELECT s.schoolNum, s.schoolName, "
 			+ "sn.netId, sn.netName, sty.typeId, sty.typeName, g.groupNum, g.groupName, "
 			+ "gt.groupTypeNum, gt.groupTypeName, a.areaNum, a.areaName, c.cityNum, c.cityName, "
 			+ "CASE WHEN g.meetingDay = 1 THEN 'ראשון' "
 			+ "WHEN g.meetingDay = 2 THEN 'שני' "
 			+ "WHEN g.meetingDay = 3 THEN 'שלישי' "
 			+ "WHEN g.meetingDay = 4 THEN 'רביעי' "
 			+ "WHEN g.meetingDay = 5 THEN 'חמישי' "
 			+ "WHEN g.meetingDay = 6 THEN 'שישי' "
 			+ "WHEN g.meetingDay = 7 THEN 'שבת' END meetingDay, "
 			+ "g.meetingTime, st.stepNum, st.stepName, p.programNum, p.programName, g.activityYear FROM schools s INNER JOIN groups g ON  "
 			+ "s.schoolNum = g.schoolNum INNER JOIN grouptypes gt ON g.groupTypeNum = gt.groupTypeNum INNER JOIN  "
 			+ "cities c ON s.cityNum = c.cityNum INNER JOIN steps st ON g.stepNum = st.stepnum INNER JOIN programs p  "
 			+ "ON g.programNum = p.programNum INNER JOIN areas a ON c.areaNum = a.areaNum "
 			+ "INNER JOIN schoolnetwork sn ON s.netId = sn.netId INNER JOIN schooltype sty ON s.typeId = sty.typeId WHERE g.activityYear =";
 
 	private static final String REPORT_GROUP_MEMBERS_LIST = "SELECT g.groupName, gm.memberId, gm.memberName, gm.memberPhone, "
 			+ "gm.memberEmail FROM groupmembers gm inner join "
 			+ "groups g on gm.schoolNum = g.schoolNum and gm.groupNum = g.groupNum inner join "
 			+ "schools s on g.schoolNum = s.schoolNum";
 
 	private static final String REPORT_GROUP_STATUS_LIST = "SELECT statusDate, details FROM groupstatuses ";
 
 	private static final String REPORT_GROUP_DETAILS = "SELECT s.schoolNum, s.schoolName, "
 			+ "sn.netId, sn.netName, sty.typeId, sty.typeName, g.groupNum, g.groupName, s.address, s.principleName, s.phone, s.fax, s.email, "
 			+ "gt.groupTypeNum, gt.groupTypeName, a.areaNum, a.areaName, c.cityNum, c.cityName, "
 			+ "CASE WHEN g.meetingDay = 1 THEN 'ראשון' "
 			+ "WHEN g.meetingDay = 2 THEN 'שני' "
 			+ "WHEN g.meetingDay = 3 THEN 'שלישי' "
 			+ "WHEN g.meetingDay = 4 THEN 'רביעי' "
 			+ "WHEN g.meetingDay = 5 THEN 'חמישי' "
 			+ "WHEN g.meetingDay = 6 THEN 'שישי' "
 			+ "WHEN g.meetingDay = 7 THEN 'שבת' END meetingDay, "
 			+ "g.meetingTime, st.stepNum, st.stepName, p.programNum, p.programName, g.activityYear FROM schools s INNER JOIN groups g ON  "
 			+ "s.schoolNum = g.schoolNum INNER JOIN grouptypes gt ON g.groupTypeNum = gt.groupTypeNum INNER JOIN  "
 			+ "cities c ON s.cityNum = c.cityNum INNER JOIN steps st ON g.stepNum = st.stepnum INNER JOIN programs p  "
 			+ "ON g.programNum = p.programNum INNER JOIN areas a ON c.areaNum = a.areaNum "
 			+ "INNER JOIN schoolnetwork sn ON s.netId = sn.netId INNER JOIN schooltype sty ON s.typeId = sty.typeId WHERE g.schoolNum = ? and g.groupNum = ?";
 
 	private static final String REPORT_SPONSOR_PAYMENTS = "SELECT paymentNum, b.bindingYear, sc.schoolNum, sc.schoolName, obligationDate, receiveDate, amount, p.comments "
 			+ "FROM sponsors s inner join payments p on s.sponsorNum = p.sponsorNum inner join bindings b on p.bindingYear = b.bindingYear "
 			+ "and p.schoolNum = b.schoolNum inner join schools sc on b.schoolNum = sc.schoolNum inner join groups g on sc.schoolNum=g.schoolNum and p.bindingYear = g.activityYear ";
 
 	private static final String REPORT_SPONSORS_DETAILS = "SELECT sponsorNum, sponsorName, contactName, contactPhone, contactMail, description FROM sponsors ";
 
 	private static final String REPORT_SPONSORS_PAYMENTS = "SELECT paymentNum, s.sponsorNum, s.sponsorName, s.contactName, s.contactPhone, s.contactMail, s.description, b.bindingYear, sc.schoolNum, sc.schoolName, obligationDate, receiveDate, amount, p.comments "
 			+ "FROM sponsors s inner join payments p on s.sponsorNum = p.sponsorNum inner join bindings b on p.bindingYear = b.bindingYear "
 			+ "and p.schoolNum = b.schoolNum inner join schools sc on b.schoolNum = sc.schoolNum inner join groups g on sc.schoolNum=g.schoolNum and p.bindingYear = g.activityYear ";
 
 	private static final String REPORT_PRODUCTS_BY_MANUFACTURER = "SELECT m.manuNum, m.name, m.address, m.contactName, m.contactPhone, m.contactMail, p.productNum, p.productName, p.Description, "
 			+ "pc.catName, g.groupName, g.activityYear, s.schoolName, c.cityName, a.areaName, sn.netName, st.typeName, gt.groupTypeName "
 			+ "FROM manufacturers m inner join products p on m.manuNum = p.manuNum inner join product_categories pc on p.catId = pc.catId "
 			+ "inner join groups g on p.groupNum = g.groupNum and p.schoolNum = g.schoolNum inner join schools s on g.schoolNum = s.schoolNum inner join cities c on s.cityNum = c.cityNum inner join areas a on c.areaNum = a.areaNum inner join schoolnetwork sn on s.netId = sn.netId "
 			+ "inner join schooltype st on s.typeId = st.typeId inner join grouptypes gt on g.groupTypeNum = gt.groupTypeNum ";
 
 	private static final String REPORT_PRODUCTS_BY_CATEGORY = "SELECT m.manuNum, m.name, p.productNum, p.productName, p.Description, "
 			+ "pc.catId, pc.catName, g.groupName, g.activityYear, s.schoolName, c.cityName, a.areaName, sn.netName, st.typeName, gt.groupTypeName "
 			+ "FROM manufacturers m inner join products p on m.manuNum = p.manuNum inner join product_categories pc on p.catId = pc.catId "
 			+ "inner join groups g on p.groupNum = g.groupNum and p.schoolNum = g.schoolNum inner join schools s on g.schoolNum = s.schoolNum inner join cities c on s.cityNum = c.cityNum inner join areas a on c.areaNum = a.areaNum inner join schoolnetwork sn on s.netId = sn.netId "
 			+ "inner join schooltype st on s.typeId = st.typeId inner join grouptypes gt on g.groupTypeNum = gt.groupTypeNum ";
 
 	private static final String REPORT_BINDING_BY_YEAR = "SELECT * FROM schools s INNER JOIN cities c ON s.cityNum=c.cityNum INNER JOIN areas a ON a.areaNum=c.areaNum "
 			+ "INNER JOIN schoolnetwork sn ON sn.netId = s.netId INNER JOIN schooltype st ON st.typeId = s.typeId "
 			+ "LEFT JOIN bindings b ON s.schoolNum=b.schoolNum and b.bindingYear=";
 
 	private static final String REPORT_INSTRUCTORS_IN_GROUPS = "SELECT * from "
 			+ "( select f.instructorName, f.occupation,f.phone, f.email, 'מורה' as insType, NULL as companyNum, NULL as companyName, groupName, activityYear, schoolName, ar.areaNum, areaName, cityName, sc.address "
 			+ "from instructors f inner join groupinstructors gi on f.instructorNum = gi.instructorNum "
 			+ "inner join groups g on gi.groupNum = g.groupNum and gi.schoolNum = g.schoolNum "
 			+ "inner join schools sc on g.schoolNum = sc.schoolNum inner join cities ci on sc.cityNum = ci.cityNum "
 			+ "inner join areas ar on ci.areaNum = ar.areaNum "
 			+ "where f.instructorNum "
 			+ "not in (select studentNum from students) "
 			+ "and f.instructorNum not in (select businessInsNum from businessinstructors) "
 			+ "union select f.instructorName,f.occupation,f.phone, f.email,'סטודנט' as insType, s.institutionNum as companyNum, i.institutionName as companyName, "
 			+ "groupName, activityYear, schoolName, ar.areaNum, areaName, cityName, sc.address from instructors f inner join groupinstructors gi on "
 			+ "f.instructorNum = gi.instructorNum inner join groups g on gi.groupNum = g.groupNum and "
 			+ "gi.schoolNum = g.schoolNum inner join schools sc on g.schoolNum = sc.schoolNum inner join "
 			+ "students s on f.instructorNum = s.studentNum "
 			+ "inner join institutions i on s.institutionNum = i.institutionNum "
 			+ "inner join cities ci on sc.cityNum = ci.cityNum "
 			+ "inner join areas ar on ci.areaNum = ar.areaNum "
 			+ "union select f.instructorName,f.occupation,f.phone,f.email, 'עסקי' as insType, c.companyNum, c.companyName, groupName, activityYear, schoolName, ar.areaNum, areaName, cityName, sc.address "
 			+ "from instructors f inner join groupinstructors gi on f.instructorNum = gi.instructorNum "
 			+ "inner join groups g on gi.groupNum = g.groupNum and gi.schoolNum = g.schoolNum inner join "
 			+ "schools sc on g.schoolNum = sc.schoolNum inner join businessinstructors bi on "
 			+ "f.instructorNum = bi.businessInsNum left join companies c on bi.companyNum = c.companyNum inner join cities ci on sc.cityNum = ci.cityNum "
 			+ "inner join areas ar on ci.areaNum = ar.areaNum) a ";
 
 	/** end of constants methods part **/
 
 	public static List<Employee> getEmployeesList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		Area area;
 		List<Employee> employees = new ArrayList<Employee>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_EMPLOYEES_LIST);
 			while (rs.next()) {
 				area = new Area(rs.getInt("areaNum"), rs.getString("areaName"));
 				employees.add(new Employee(rs.getString("username"), rs
 						.getString("name"), rs.getString("phone"), rs
 						.getString("email"), rs.getString("password"), area, rs
 						.getString("rolename")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return employees;
 	}
 
 	public static List<Employee> getEmployeesByAreaList(String areaNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Area area;
 		List<Employee> employees = new ArrayList<Employee>();
 		try {
 			statement = connection.prepareStatement(GET_EMPLOYEES_LIST
 					+ " WHERE a.areaNum = ?");
 			statement.setInt(1, Integer.parseInt(areaNum));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				area = new Area(rs.getInt("areaNum"), rs.getString("areaName"));
 				employees.add(new Employee(rs.getString("username"), rs
 						.getString("name"), rs.getString("phone"), rs
 						.getString("email"), rs.getString("password"), area, rs
 						.getString("rolename")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return employees;
 	}
 
 	public static Employee getEmployeDetails(String username) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Area area;
 		Employee employee = null;
 		try {
 			statement = connection.prepareStatement(GET_EMPLOYEE_DETAILS);
 			statement.setString(1, username);
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				area = new Area(rs.getInt("areaNum"), rs.getString("areaName"));
 				employee = new Employee(rs.getString("username"),
 						rs.getString("name"), rs.getString("phone"),
 						rs.getString("email"), rs.getString("password"), area);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return employee;
 	}
 
 	public static List<School> getSchoolsList(Integer areaNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Area area;
 		City city;
 		School school;
 		List<School> schools = new ArrayList<School>();
 		try {
 			if (areaNum == 0) {
 				statement = connection.prepareStatement(GET_SCHOOLS_LIST
 						+ " ORDER BY c.cityName, s.schoolName");
 			} else {
 				statement = connection.prepareStatement(GET_SCHOOLS_IN_AREA
 						+ " ORDER BY c.cityName, s.schoolName");
 				statement.setInt(1, areaNum);
 			}
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				area = new Area(rs.getInt("areaNum"), rs.getString("areaName"));
 				city = new City(rs.getInt("cityNum"), rs.getString("cityName"),
 						area);
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolname"), city,
 						rs.getString("address"), rs.getString("principleName"),
 						rs.getString("phone"), rs.getString("fax"),
 						rs.getString("email"), rs.getString("contactName"),
 						rs.getString("contactPhone"),
 						rs.getString("contactMail"), rs.getInt("netId"),
 						rs.getInt("typeId"), rs.getString("comments"));
 				schools.add(school);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return schools;
 	}
 
 	public static List<School> getSchoolsByCity(String cityNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Area area;
 		City city;
 		School school;
 		List<School> schools = new ArrayList<School>();
 		try {
 			statement = connection.prepareStatement(GET_SCHOOLS_IN_CITY);
 			statement.setInt(1, Integer.parseInt(cityNum));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				area = new Area(rs.getInt("areaNum"), rs.getString("areaName"));
 				city = new City(rs.getInt("cityNum"), rs.getString("cityName"),
 						area);
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolname"), city,
 						rs.getString("address"), rs.getString("principleName"),
 						rs.getString("phone"), rs.getString("fax"),
 						rs.getString("email"), rs.getString("contactName"),
 						rs.getString("contactPhone"),
 						rs.getString("contactMail"), rs.getInt("netId"),
 						rs.getInt("typeId"), rs.getString("comments"));
 				schools.add(school);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return schools;
 	}
 
 	public static List<School> getSchoolsByNetwork(String netNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Area area;
 		City city;
 		School school;
 		List<School> schools = new ArrayList<School>();
 		try {
 			statement = connection.prepareStatement(GET_SCHOOLS_BY_NETWORK);
 			statement.setInt(1, Integer.parseInt(netNum));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				area = new Area(rs.getInt("areaNum"), rs.getString("areaName"));
 				city = new City(rs.getInt("cityNum"), rs.getString("cityName"),
 						area);
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolname"), city,
 						rs.getString("address"), rs.getString("principleName"),
 						rs.getString("phone"), rs.getString("fax"),
 						rs.getString("email"), rs.getString("contactName"),
 						rs.getString("contactPhone"),
 						rs.getString("contactMail"), rs.getInt("netId"),
 						rs.getInt("typeId"), rs.getString("comments"));
 				schools.add(school);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return schools;
 	}
 
 	public static List<School> getSchoolsByType(String typeNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Area area;
 		City city;
 		School school;
 		List<School> schools = new ArrayList<School>();
 		try {
 			statement = connection.prepareStatement(GET_SCHOOLS_BY_TYPE);
 			statement.setInt(1, Integer.parseInt(typeNum));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				area = new Area(rs.getInt("areaNum"), rs.getString("areaName"));
 				city = new City(rs.getInt("cityNum"), rs.getString("cityName"),
 						area);
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolname"), city,
 						rs.getString("address"), rs.getString("principleName"),
 						rs.getString("phone"), rs.getString("fax"),
 						rs.getString("email"), rs.getString("contactName"),
 						rs.getString("contactPhone"),
 						rs.getString("contactMail"), rs.getInt("netId"),
 						rs.getInt("typeId"), rs.getString("comments"));
 				schools.add(school);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return schools;
 	}
 
 	public static School getSchoolByNum(String schoolNum) {
 		Connection connection = DBConnection.getConnection();
 		if (schoolNum == null)
 			return null;
 		PreparedStatement statement;
 		Area area;
 		City city;
 		School school = null;
 		try {
 			statement = connection.prepareStatement(GET_SCHOOL_BY_NUM);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				area = new Area(rs.getInt("areaNum"), rs.getString("areaName"));
 				city = new City(rs.getInt("cityNum"), rs.getString("cityName"),
 						area);
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolname"), city,
 						rs.getString("address"), rs.getString("principleName"),
 						rs.getString("phone"), rs.getString("fax"),
 						rs.getString("email"), rs.getString("contactName"),
 						rs.getString("contactPhone"),
 						rs.getString("contactMail"), rs.getInt("netId"),
 						rs.getInt("typeId"), rs.getString("comments"));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return school;
 	}
 
 	public static List<School> getSchoolsWithMarketingStatus(int year,
 			int areaNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement = null;
 		Area area;
 		City city;
 		Binding binding;
 		School school;
 		List<School> schools = new ArrayList<School>();
 		try {
 			if (areaNum == 0) {
 				statement = connection
 						.prepareStatement(GET_SCHOOLS_WITH_MARKETING_STATUS
 								+ " ORDER BY a.areaName, c.cityName, s.schoolName");
 				statement.setInt(1, year);
 			} else {
 				statement = connection
 						.prepareStatement(GET_SCHOOLS_WITH_MARKETING_STATUS
 								+ " WHERE a.areaNum=? ORDER BY a.areaName, c.cityName, s.schoolName");
 				statement.setInt(1, year);
 				statement.setInt(2, areaNum);
 			}
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				area = new Area(rs.getInt("areaNum"), rs.getString("areaName"));
 				city = new City(rs.getInt("cityNum"), rs.getString("cityName"),
 						area);
 				binding = new Binding(rs.getInt("bindingYear"),
 						rs.getString("marketingStatus"));
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolname"), city,
 						rs.getString("address"), rs.getString("principleName"),
 						rs.getString("phone"), rs.getString("fax"),
 						rs.getString("email"), rs.getString("contactName"),
 						rs.getString("contactPhone"),
 						rs.getString("contactMail"), rs.getInt("netId"),
 						rs.getInt("typeId"), rs.getString("comments"), binding);
 				schools.add(school);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return schools;
 	}
 
 	public static void replaceMarketingStatus(int schoolNum, int year,
 			String oldStatus, String newStatus) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			if (newStatus.equalsIgnoreCase("NONE")) {
 				deleteBinding(Integer.toString(schoolNum),
 						Integer.toString(year));
 			}
 			if (oldStatus.equalsIgnoreCase("NONE")) {
 				statement = connection.prepareStatement(REPLACE_EMPTY_BINDING);
 				statement.setInt(1, schoolNum);
 				statement.setInt(2, year);
 				statement.setString(3, MarketingStatus.valueOf(newStatus)
 						.toString());
 				statement.executeUpdate();
 			} else {
 				statement = connection
 						.prepareStatement(UPDATE_MARKETING_STATUS);
 				statement.setString(1, MarketingStatus.valueOf(newStatus)
 						.toString());
 				statement.setInt(2, schoolNum);
 				statement.setInt(3, year);
 				statement.executeUpdate();
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateSchool(int schoolNum, String schoolName,
 			String address, String principleName, String phone, String fax,
 			String email, String contactName, String contactPhone,
			String contactMail, int cityNum, Integer netId, int typeId,
			String comments) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_SCHOOL);
 			statement.setString(1, schoolName);
 			statement.setString(2, address);
 			statement.setString(3, principleName);
 			statement.setString(4, phone);
 			statement.setString(5, fax);
 			statement.setString(6, email);
 			statement.setString(7, contactName);
 			statement.setString(8, contactPhone);
 			statement.setString(9, contactMail);
 			statement.setInt(10, cityNum);
 			if (netId != null) {
 				statement.setInt(11, netId);
 			} else {
 				statement.setNull(11, Types.INTEGER);
 			}
 			statement.setInt(12, typeId);
 			statement.setString(13, comments);
 			statement.setInt(14, schoolNum);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static int insertSchool(String schoolName, String address,
 			String principleName, String phone, String fax, String email,
 			String contactName, String contactPhone, String contactMail,
 			int cityNum, Integer netId, int typeId, String comments) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		int schoolNum = -1;
 		try {
 			statement = connection.prepareStatement(INSERT_SCHOOL,
 					PreparedStatement.RETURN_GENERATED_KEYS);
 			statement.setString(1, schoolName);
 			statement.setString(2, address);
 			statement.setString(3, principleName);
 			statement.setString(4, phone);
 			statement.setString(5, fax);
 			statement.setString(6, email);
 			statement.setString(7, contactName);
 			statement.setString(8, contactPhone);
 			statement.setString(9, contactMail);
 			statement.setInt(10, cityNum);
 			if (netId != null) {
 				statement.setInt(11, netId);
 			} else {
 				statement.setNull(11, Types.INTEGER);
 			}
 			statement.setInt(12, typeId);
 			statement.setString(13, comments);
 			statement.executeUpdate();
 			ResultSet rs = statement.getGeneratedKeys();
 			if (rs.next()) {
 				schoolNum = rs.getInt("GENERATED_KEY");
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return schoolNum;
 	}
 
 	public static void deleteSchool(int schoolNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_SCHOOL);
 			statement.setInt(1, schoolNum);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static List<City> getCitiesList(int areaNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		List<City> cities = new ArrayList<City>();
 		try {
 			if (areaNum == 0)
 				statement = connection.prepareStatement(GET_CITIES_LIST
 						+ " ORDER BY a.areaName, cityName");
 			else {
 				statement = connection.prepareStatement(GET_CITIES_LIST
 						+ " WHERE a.areaNum=? ORDER BY a.areaName, cityName");
 				statement.setInt(1, areaNum);
 			}
 
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				cities.add(new City(rs.getInt("cityNum"), rs
 						.getString("cityName"), new Area(rs.getInt("areaNum"),
 						rs.getString("areaName"))));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return cities;
 	}
 
 	public static List<GroupType> getGroupTypesList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		List<GroupType> types = new ArrayList<GroupType>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_GROUP_TYPES_LIST);
 			while (rs.next()) {
 				types.add(new GroupType(rs.getInt("groupTypeNum"), rs
 						.getString("groupTypeName")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return types;
 	}
 
 	public static GroupType getGroupType(int groupTypeNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		GroupType groupType = null;
 		try {
 			statement = connection.prepareStatement(GET_GROUP_TYPES_LIST
 					+ " WHERE groupTypeNum =? ");
 			statement.setInt(1, groupTypeNum);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				groupType = new GroupType(rs.getInt("groupTypeNum"),
 						rs.getString("groupTypeName"),
 						rs.getString("contactName"),
 						rs.getString("contactPhone"),
 						rs.getString("contactMail"));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return groupType;
 	}
 
 	public static void updateGroupType(int groupTypeNum, GroupType groupType) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_GROUP_TYPE);
 			statement.setString(1, groupType.getContactName());
 			statement.setString(2, groupType.getContactPhone());
 			statement.setString(3, groupType.getContactMail());
 			statement.setInt(4, groupTypeNum);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static List<Company> getCompaniesList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		List<Company> companies = new ArrayList<Company>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_COMPANIES_LIST);
 			while (rs.next()) {
 				companies.add(new Company(rs.getInt("companyNum"), rs
 						.getString("companyName")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return companies;
 	}
 
 	public static List<AcademicInstitution> getInstitutionsList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		List<AcademicInstitution> institutions = new ArrayList<AcademicInstitution>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement
 					.executeQuery(GET_ACADEMIC_INSTITUTIONS_LIST);
 			while (rs.next()) {
 				institutions.add(new AcademicInstitution(rs
 						.getInt("institutionNum"), rs
 						.getString("institutionName")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return institutions;
 	}
 
 	public static List<Network> getNetworkList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		List<Network> networks = new ArrayList<Network>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_NETWORKS_LIST);
 			while (rs.next()) {
 				networks.add(new Network(rs.getInt("netId"), rs
 						.getString("netName")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return networks;
 	}
 
 	public static List<SchoolType> getSchoolTypesList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		List<SchoolType> types = new ArrayList<SchoolType>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_SCHOOL_TYPES_LIST);
 			while (rs.next()) {
 				types.add(new SchoolType(rs.getInt("typeId"), rs
 						.getString("typeName")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return types;
 	}
 
 	public static List<Program> getProgramsList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		List<Program> programs = new ArrayList<Program>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_PROGRAMS_LIST);
 			while (rs.next()) {
 				programs.add(new Program(rs.getInt("programNum"), rs
 						.getString("programName")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return programs;
 	}
 
 	public static Program getProgramByGroup(Integer schoolNum, Integer groupNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Program program = null;
 		try {
 			statement = connection.prepareStatement(GET_PROGRAM_BY_GROUP);
 			statement.setInt(1, schoolNum);
 			statement.setInt(2, groupNum);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				program = new Program(rs.getInt("programNum"),
 						rs.getString("programName"));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return program;
 	}
 
 	public static List<Step> getStepsList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		List<Step> steps = new ArrayList<Step>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_STEPS_LIST);
 			while (rs.next()) {
 				steps.add(new Step(rs.getInt("stepNum"), rs
 						.getString("stepName")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return steps;
 	}
 
 	public static List<Group> getGroupsList(int year, int areaNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		City city;
 		School school;
 		Step step;
 		GroupType type;
 		Program program;
 		Group group;
 		List<Group> groups = new ArrayList<Group>();
 		try {
 			if (areaNum == 0) {
 				statement = connection
 						.prepareStatement(GET_GROUPS_LIST_BY_YEAR);
 				statement.setInt(1, year);
 			} else {
 				statement = connection.prepareStatement(GET_GROUPS_LIST_BY_YEAR
 						+ " and c.areaNum =? ");
 				statement.setInt(1, year);
 				statement.setInt(2, areaNum);
 			}
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				city = new City(
 						rs.getInt("cityNum"),
 						rs.getString("cityName"),
 						new Area(rs.getInt("areaNum"), rs.getString("areaName")));
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolName"), city);
 				step = new Step(rs.getInt("stepNum"), rs.getString("stepName"));
 				type = new GroupType(rs.getInt("groupTypeNum"),
 						rs.getString("groupTypeName"));
 				program = new Program(rs.getInt("programNum"),
 						rs.getString("programName"));
 				group = new Group(rs.getInt("groupNum"),
 						rs.getString("groupName"), rs.getInt("activityYear"),
 						rs.getInt("meetingDay"), rs.getTime("meetingTime"),
 						school, step, type, program);
 				groups.add(group);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return groups;
 	}
 
 	public static List<Group> getAllGroupsList() {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		City city;
 		School school;
 		Step step;
 		GroupType type;
 		Program program;
 		Group group;
 		List<Group> groups = new ArrayList<Group>();
 		try {
 			statement = connection.prepareStatement(GET_GROUPS_LIST);
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				city = new City(
 						rs.getInt("cityNum"),
 						rs.getString("cityName"),
 						new Area(rs.getInt("areaNum"), rs.getString("areaName")));
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolName"), city);
 				step = new Step(rs.getInt("stepNum"), rs.getString("stepName"));
 				type = new GroupType(rs.getInt("groupTypeNum"),
 						rs.getString("groupTypeName"));
 				program = new Program(rs.getInt("programNum"),
 						rs.getString("programName"));
 				group = new Group(rs.getInt("groupNum"),
 						rs.getString("groupName"), rs.getInt("activityYear"),
 						rs.getInt("meetingDay"), rs.getTime("meetingTime"),
 						school, step, type, program);
 				groups.add(group);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return groups;
 	}
 
 	public static List<Group> getGroupsByType(String groupTypeNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		City city;
 		School school;
 		Step step;
 		GroupType type;
 		Program program;
 		Group group;
 		List<Group> groups = new ArrayList<Group>();
 		try {
 			statement = connection.prepareStatement(GET_GROUPS_LIST
 					+ " WHERE g.groupTypeNum=?");
 			statement.setInt(1, Integer.parseInt(groupTypeNum));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				city = new City(
 						rs.getInt("cityNum"),
 						rs.getString("cityName"),
 						new Area(rs.getInt("areaNum"), rs.getString("areaName")));
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolName"), city);
 				step = new Step(rs.getInt("stepNum"), rs.getString("stepName"));
 				type = new GroupType(rs.getInt("groupTypeNum"),
 						rs.getString("groupTypeName"));
 				program = new Program(rs.getInt("programNum"),
 						rs.getString("programName"));
 				group = new Group(rs.getInt("groupNum"),
 						rs.getString("groupName"), rs.getInt("activityYear"),
 						rs.getInt("meetingDay"), rs.getTime("meetingTime"),
 						school, step, type, program);
 				groups.add(group);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return groups;
 	}
 
 	public static List<Group> getGroupsByProgram(String programNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		City city;
 		School school;
 		Step step;
 		GroupType type;
 		Program program;
 		Group group;
 		List<Group> groups = new ArrayList<Group>();
 		try {
 			statement = connection.prepareStatement(GET_GROUPS_LIST
 					+ " WHERE g.programNum=?");
 			statement.setInt(1, Integer.parseInt(programNum));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				city = new City(
 						rs.getInt("cityNum"),
 						rs.getString("cityName"),
 						new Area(rs.getInt("areaNum"), rs.getString("areaName")));
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolName"), city);
 				step = new Step(rs.getInt("stepNum"), rs.getString("stepName"));
 				type = new GroupType(rs.getInt("groupTypeNum"),
 						rs.getString("groupTypeName"));
 				program = new Program(rs.getInt("programNum"),
 						rs.getString("programName"));
 				group = new Group(rs.getInt("groupNum"),
 						rs.getString("groupName"), rs.getInt("activityYear"),
 						rs.getInt("meetingDay"), rs.getTime("meetingTime"),
 						school, step, type, program);
 				groups.add(group);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return groups;
 	}
 
 	public static List<Group> getGroupsByStep(String stepNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		City city;
 		School school;
 		Step step;
 		GroupType type;
 		Program program;
 		Group group;
 		List<Group> groups = new ArrayList<Group>();
 		try {
 			statement = connection.prepareStatement(GET_GROUPS_LIST
 					+ " WHERE g.stepNum=?");
 			statement.setInt(1, Integer.parseInt(stepNum));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				city = new City(
 						rs.getInt("cityNum"),
 						rs.getString("cityName"),
 						new Area(rs.getInt("areaNum"), rs.getString("areaName")));
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolName"), city);
 				step = new Step(rs.getInt("stepNum"), rs.getString("stepName"));
 				type = new GroupType(rs.getInt("groupTypeNum"),
 						rs.getString("groupTypeName"));
 				program = new Program(rs.getInt("programNum"),
 						rs.getString("programName"));
 				group = new Group(rs.getInt("groupNum"),
 						rs.getString("groupName"), rs.getInt("activityYear"),
 						rs.getInt("meetingDay"), rs.getTime("meetingTime"),
 						school, step, type, program);
 				groups.add(group);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return groups;
 	}
 
 	public static List<Group> getSchoolGroupsList(Integer schoolNum) {
 		if (schoolNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		City city;
 		School school;
 		Step step;
 		GroupType type;
 		Program program;
 		Group group;
 		List<Group> groups = new ArrayList<Group>();
 		try {
 			statement = connection.prepareStatement(GET_SCHOOL_GROUPS_LIST);
 			statement.setInt(1, schoolNum);
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				city = new City(rs.getInt("cityNum"), rs.getString("cityName"));
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolName"), city);
 				step = new Step(rs.getInt("stepNum"), rs.getString("stepName"));
 				type = new GroupType(rs.getInt("groupTypeNum"),
 						rs.getString("groupTypeName"));
 				program = new Program(rs.getInt("programNum"),
 						rs.getString("programName"));
 				group = new Group(rs.getInt("groupNum"),
 						rs.getString("groupName"), rs.getInt("activityYear"),
 						rs.getInt("meetingDay"), rs.getTime("meetingTime"),
 						school, step, type, program);
 				groups.add(group);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return groups;
 	}
 
 	public static Group getGroupByNum(String schoolNum, String groupNum) {
 		if (schoolNum == null || groupNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		City city;
 		School school;
 		Step step;
 		GroupType type;
 		Program program;
 		Group group = null;
 		try {
 			statement = connection.prepareStatement(GET_GROUP_BY_NUM);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(groupNum));
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				city = new City(rs.getInt("cityNum"), rs.getString("cityName"));
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolName"), city);
 				step = new Step(rs.getInt("stepNum"), rs.getString("stepName"));
 				type = new GroupType(rs.getInt("groupTypeNum"),
 						rs.getString("groupTypeName"));
 				program = new Program(rs.getInt("programNum"),
 						rs.getString("programName"));
 				group = new Group(rs.getInt("groupNum"),
 						rs.getString("groupName"), rs.getInt("activityYear"),
 						rs.getInt("meetingDay"), rs.getTime("meetingTime"),
 						school, step, type, program);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return group;
 	}
 
 	public static List<GroupMember> getGroupMembersList(Integer schoolNum,
 			Integer groupNum) {
 		if (schoolNum == null || groupNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		GroupMember member;
 		List<GroupMember> members = new ArrayList<GroupMember>();
 		try {
 			statement = connection.prepareStatement(GET_GROUP_MEMBERS_LIST);
 			statement.setInt(1, schoolNum);
 			statement.setInt(2, groupNum);
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				member = new GroupMember(rs.getString("memberId"),
 						rs.getString("memberName"),
 						rs.getString("memberPhone"),
 						rs.getString("memberEmail"));
 				members.add(member);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return members;
 	}
 
 	public static void deleteGroupMember(int schoolNum, int groupNum,
 			String memberId) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_GROUP_MEMBER);
 			statement.setInt(1, schoolNum);
 			statement.setInt(2, groupNum);
 			statement.setString(3, memberId);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteGroupMembers(String schoolNum, String groupNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_GROUP_MEMBERS);
 			statement.setString(1, schoolNum);
 			statement.setString(2, groupNum);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void replaceGroupMember(int schoolNum, int groupNum,
 			String memberId, String name, String phone, String email) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(REPLACE_GROUP_MEMBER);
 			statement.setInt(1, schoolNum);
 			statement.setInt(2, groupNum);
 			statement.setString(3, memberId);
 			statement.setString(4, name);
 			statement.setString(5, phone);
 			statement.setString(6, email);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static BankDetails getGroupBankDetails(Integer schoolNum,
 			Integer groupNum) {
 		if (schoolNum == null || groupNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		BankDetails bankDetails = null;
 		try {
 			statement = connection.prepareStatement(GET_GROUP_BANK_DETAILS);
 			statement.setInt(1, schoolNum);
 			statement.setInt(2, groupNum);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				bankDetails = new BankDetails(rs.getDate("openAccountDate"),
 						rs.getString("branchNum"), rs.getString("branchName"),
 						rs.getString("accountNum"));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return bankDetails;
 	}
 
 	public static void updateBankDetails(int schoolNum, int groupNum,
 			BankDetails bankDetails) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_BANK_DETAILS);
 			if (bankDetails.getOpenAccountDate() != null
 					&& bankDetails.getOpenAccountDate().toString() != "")
 				statement.setDate(1, bankDetails.getOpenAccountDate());
 			else
 				statement.setNull(1, Types.DATE);
 
 			statement.setString(2, bankDetails.getBranchNum());
 			statement.setString(3, bankDetails.getBranchName());
 			statement.setString(4, bankDetails.getAccountNum());
 			statement.setInt(5, schoolNum);
 			statement.setInt(6, groupNum);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static List<Area> getAreasList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		List<Area> areas = new ArrayList<Area>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_AREAS_LIST);
 			while (rs.next()) {
 				areas.add(new Area(rs.getInt("areaNum"), rs
 						.getString("areaName")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return areas;
 	}
 
 	public static Area getAreaByNum(Integer areaNum) {
 		if (areaNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Area area = null;
 		try {
 			statement = connection.prepareStatement(GET_AREA_BY_NUM);
 			statement.setInt(1, areaNum);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				area = new Area(areaNum, rs.getString("areaName"));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return area;
 	}
 
 	public static void deleteEmployee(String username) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement deleteEmployee;
 		PreparedStatement deleteEmployeeRole;
 		try {
 			connection.setAutoCommit(false);
 			deleteEmployee = connection.prepareStatement(DELETE_EMPLOYEE);
 			deleteEmployee.setString(1, username);
 			deleteEmployee.executeUpdate();
 			deleteEmployeeRole = connection
 					.prepareStatement(DELETE_EMPLOYEE_ROLE);
 			deleteEmployeeRole.setString(1, username);
 			deleteEmployeeRole.executeUpdate();
 			connection.setAutoCommit(true);
 		} catch (Exception e) {
 			try {
 				connection.rollback();
 			} catch (SQLException rollbackEx) {
 				System.err.println("Exception: " + e.getMessage());
 				rollbackEx.printStackTrace();
 			}
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void replaceEmployee(String username, String name,
 			String phone, String email, String password, Area area, String role) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement replaceEmployee;
 		PreparedStatement replaceEmployeeRole;
 		try {
 			connection.setAutoCommit(false);
 			if (DBQueries.getEmployeDetails(username) == null) {
 				replaceEmployee = connection.prepareStatement(INSERT_EMPLOYEE);
 				replaceEmployee.setString(1, username);
 				replaceEmployee.setString(2, name);
 				replaceEmployee.setString(3, phone);
 				replaceEmployee.setString(4, email);
 				replaceEmployee.setString(5, password);
 				if (area == null) {
 					replaceEmployee.setNull(6, Types.INTEGER);
 				} else {
 					replaceEmployee.setInt(6, area.getAreaNum());
 				}
 			} else {
 				replaceEmployee = connection.prepareStatement(UPDATE_EMPLOYEE);
 				replaceEmployee.setString(1, name);
 				replaceEmployee.setString(2, phone);
 				replaceEmployee.setString(3, email);
 				replaceEmployee.setString(4, password);
 				if (area == null) {
 					replaceEmployee.setNull(5, Types.INTEGER);
 				} else {
 					replaceEmployee.setInt(5, area.getAreaNum());
 				}
 				replaceEmployee.setString(6, username);
 			}
 
 			replaceEmployee.executeUpdate();
 			replaceEmployeeRole = connection
 					.prepareStatement(REPLACE_EMPLOYEE_ROLE);
 			replaceEmployeeRole.setString(1, username);
 			replaceEmployeeRole.setString(2, role);
 			replaceEmployeeRole.executeUpdate();
 			connection.setAutoCommit(true);
 		} catch (Exception e) {
 			try {
 				connection.rollback();
 			} catch (SQLException rollbackEx) {
 				System.err.println("Exception: " + e.getMessage());
 				rollbackEx.printStackTrace();
 			}
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static List<Instructor> getInstructorsList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		Instructor instructor;
 		List<Instructor> instructors = new ArrayList<Instructor>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_INSTRUCTORS_LIST);
 			while (rs.next()) {
 				instructor = new Instructor(rs.getInt("instructorNum"),
 						rs.getString("instructorName"),
 						rs.getString("address"), rs.getString("phone"),
 						rs.getString("email"), rs.getString("occupation"),
 						rs.getDate("birthdate"), rs.getString("education"),
 						rs.getString("instructingExp"),
 						rs.getString("professionalExp"),
 						rs.getString("hobbies"), rs.getString("howCame"),
 						rs.getString("volunteerExp"),
 						rs.getString("expectations"), rs.getString("skills"),
 						rs.getString("impressions"), new Company(
 								rs.getInt("companyNum"),
 								rs.getString("companyName")),
 						InstructorTypes.fromChar(rs.getString("insType")
 								.charAt(0)));
 				instructors.add(instructor);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return instructors;
 	}
 
 	public static List<Instructor> getInstructorsByCompanyOrInstitution(
 			String num) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Instructor instructor;
 		List<Instructor> instructors = new ArrayList<Instructor>();
 		try {
 			statement = connection.prepareStatement(GET_INSTRUCTORS_LIST
 					+ " WHERE companyNum=? ");
 			statement.setInt(1, Integer.parseInt(num));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				instructor = new Instructor(rs.getInt("instructorNum"),
 						rs.getString("instructorName"),
 						rs.getString("address"), rs.getString("phone"),
 						rs.getString("email"), rs.getString("occupation"),
 						rs.getDate("birthdate"), rs.getString("education"),
 						rs.getString("instructingExp"),
 						rs.getString("professionalExp"),
 						rs.getString("hobbies"), rs.getString("howCame"),
 						rs.getString("volunteerExp"),
 						rs.getString("expectations"), rs.getString("skills"),
 						rs.getString("impressions"), new Company(
 								rs.getInt("companyNum"),
 								rs.getString("companyName")),
 						InstructorTypes.fromChar(rs.getString("insType")
 								.charAt(0)));
 				instructors.add(instructor);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return instructors;
 	}
 
 	public static List<Instructor> getGroupInstructorsList(String schoolNum,
 			String groupNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Instructor instructor;
 		List<Instructor> instructors = new ArrayList<Instructor>();
 		try {
 			statement = connection.prepareStatement(GET_GROUP_INSTRUCTORS_LIST);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(groupNum));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				instructor = new Instructor(rs.getInt("instructorNum"),
 						rs.getString("instructorName"),
 						rs.getString("address"), rs.getString("phone"),
 						rs.getString("email"), rs.getString("occupation"),
 						rs.getDate("birthdate"), rs.getString("education"),
 						rs.getString("instructingExp"),
 						rs.getString("professionalExp"),
 						rs.getString("hobbies"), rs.getString("howCame"),
 						rs.getString("volunteerExp"),
 						rs.getString("expectations"), rs.getString("skills"),
 						rs.getString("impressions"),
 						InstructorTypes.fromChar(rs.getString("insType")
 								.charAt(0)));
 				instructors.add(instructor);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return instructors;
 	}
 
 	public static Manufacturer getManufacturerByNum(String num) {
 		if (num == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		int manuNum = Integer.parseInt(num);
 		PreparedStatement statement;
 		Manufacturer manu = null;
 		try {
 			statement = connection.prepareStatement(GET_MANUFACTURER_BY_NUM);
 			statement.setInt(1, manuNum);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				manu = new Manufacturer(rs.getInt("manuNum"),
 						rs.getString("name"), rs.getString("address"),
 						rs.getString("contactName"),
 						rs.getString("contactPhone"),
 						rs.getString("contactMail"));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return manu;
 	}
 
 	public static Product getProductByGroup(String schoolNum, String groupNum) {
 		if (schoolNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Group group;
 		Manufacturer manufacturer;
 		ProductCategory category;
 		Product product = null;
 		try {
 			statement = connection.prepareStatement(GET_PRODUCT_BY_GROUP);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(groupNum));
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				group = new Group(Integer.parseInt(groupNum),
 						rs.getString("groupName"));
 				manufacturer = new Manufacturer(Integer.parseInt(rs
 						.getString("manuNum")));
 				category = new ProductCategory(rs.getInt("catId"));
 				product = new Product(Integer.parseInt(rs
 						.getString("productNum")), group,
 						rs.getString("productName"),
 						rs.getString("description"), rs.getString("imageLink"),
 						manufacturer, category,
 						rs.getBoolean("sentProductForm"),
 						rs.getDate("productFormSentDate"),
 						rs.getBoolean("gotProductForm"),
 						rs.getDate("productFormGotDate"));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return product;
 	}
 
 	public static List<Product> getProductByCategory(String catId) {
 		if (catId == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Group group;
 		Manufacturer manufacturer;
 		ProductCategory category;
 		List<Product> products = new ArrayList<Product>();
 		try {
 			statement = connection.prepareStatement(GET_PRODUCT_BY_CATEGORY);
 			statement.setInt(1, Integer.parseInt(catId));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				group = new Group(rs.getInt("groupNum"),
 						rs.getString("groupName"));
 				manufacturer = new Manufacturer(Integer.parseInt(rs
 						.getString("manuNum")));
 				category = new ProductCategory(rs.getInt("catId"));
 				products.add(new Product(Integer.parseInt(rs
 						.getString("productNum")), group, rs
 						.getString("productName"), rs.getString("description"),
 						rs.getString("imageLink"), manufacturer, category, rs
 								.getBoolean("sentProductForm"), rs
 								.getDate("productFormSentDate"), rs
 								.getBoolean("gotProductForm"), rs
 								.getDate("productFormGotDate")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return products;
 	}
 
 	public static List<Manufacturer> getManufacturersList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		List<Manufacturer> manues = new ArrayList<Manufacturer>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_MANUFACTURERS_LIST);
 			while (rs.next()) {
 				manues.add(new Manufacturer(rs.getInt("manuNum"), rs
 						.getString("name"), rs.getString("address"), rs
 						.getString("contactName"),
 						rs.getString("contactPhone"), rs
 								.getString("contactMail")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return manues;
 	}
 
 	public static List<ProductCategory> getProductCategoriesList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		List<ProductCategory> categories = new ArrayList<ProductCategory>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_PRODUCT_CATEGORIES_LIST);
 			while (rs.next()) {
 				categories.add(new ProductCategory(rs.getInt("catId"), rs
 						.getString("catName")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return categories;
 	}
 
 	public static ProductCategory getProductCategoryByNum(Integer catId) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		ProductCategory category = null;
 		try {
 			statement = connection
 					.prepareStatement(GET_PRODUCT_CATEGORY_BY_NUM);
 			statement.setInt(1, catId);
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				category = new ProductCategory(rs.getInt("catId"),
 						rs.getString("catName"));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return category;
 	}
 
 	public static ResultSet getCityName() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_CITY_NAME);
 			if (rs != null)
 				return rs;
 
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return null;
 	}
 
 	public static void insertProduct(String schoolNum, String groupNum,
 			String productNum, String productName, String description,
 			String imageLink, int manuNum, boolean sentProductForm,
 			Date productFormSentDate, boolean gotProductForm,
 			Date productFormGotDate, int catId) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_PRODUCT);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(groupNum));
 			statement.setInt(3, Integer.parseInt(productNum));
 			statement.setString(4, productName);
 			statement.setString(5, description);
 			statement.setString(6, imageLink);
 			statement.setInt(7, manuNum);
 			statement.setBoolean(8, sentProductForm);
 			statement.setDate(9, productFormSentDate);
 			statement.setBoolean(10, gotProductForm);
 			statement.setDate(11, productFormGotDate);
 			statement.setInt(12, catId);
 			statement.executeUpdate();
 
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateProduct(String schoolNum, String groupNum,
 			String productName, String description, String imageLink,
 			int manuNum, boolean sentProductForm, Date productFormSentDate,
 			boolean gotProductForm, Date productFormGotDate, int catId) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_PRODUCT_DETAILS);
 			statement.setString(1, productName);
 			statement.setString(2, description);
 			statement.setString(3, imageLink);
 			statement.setInt(4, manuNum);
 			statement.setBoolean(5, sentProductForm);
 			statement.setDate(6, productFormSentDate);
 			statement.setBoolean(7, gotProductForm);
 			statement.setDate(8, productFormGotDate);
 			statement.setInt(9, catId);
 			statement.setInt(10, Integer.parseInt(schoolNum));
 			statement.setInt(11, Integer.parseInt(groupNum));
 			statement.executeUpdate();
 
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteProduct(String schoolNum, String groupNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_PRODUCT);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(groupNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static List<Sponsor> getSponsorsList() {
 		Connection connection = DBConnection.getConnection();
 		Statement statement;
 		List<Sponsor> sponsors = new ArrayList<Sponsor>();
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(GET_SPONSORS_LIST);
 			while (rs.next()) {
 				sponsors.add(new Sponsor(rs.getInt("sponsorNum"), rs
 						.getString("sponsorName"), rs.getString("contactName"),
 						rs.getString("contactPhone"), rs
 								.getString("contactMail"), rs
 								.getString("description")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return sponsors;
 	}
 
 	public static Sponsor getSponsorByNum(String sponsorNum) {
 		if (sponsorNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Sponsor sponsor = null;
 		try {
 			statement = connection.prepareStatement(GET_SPONSOR_BY_NUM);
 			statement.setInt(1, Integer.parseInt(sponsorNum));
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				sponsor = new Sponsor(rs.getInt("sponsorNum"),
 						rs.getString("sponsorName"),
 						rs.getString("contactName"),
 						rs.getString("contactPhone"),
 						rs.getString("contactMail"),
 						rs.getString("description"));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return sponsor;
 	}
 
 	public static List<Payment> getSponsorPayments(String sponsorNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		List<Payment> payments = new ArrayList<Payment>();
 		try {
 			statement = connection.prepareStatement(GET_SPONSOR_PAYMENTS);
 			statement.setInt(1, Integer.parseInt(sponsorNum));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				payments.add(new Payment(rs.getInt("paymentNum"), rs
 						.getInt("schoolNum"), rs.getInt("groupNum"), rs
 						.getInt("bindingYear"), rs.getDate("obligationDate"),
 						rs.getDate("receiveDate"), rs.getDouble("amount"), rs
 								.getString("comments")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return payments;
 	}
 
 	public static void insertSponsor(Sponsor sponsor) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_SPONSOR);
 			statement.setString(1, sponsor.getSponsorName());
 			statement.setString(2, sponsor.getContactName());
 			statement.setString(3, sponsor.getContactPhone());
 			statement.setString(4, sponsor.getContactMail());
 			statement.setString(5, sponsor.getDescription());
 			statement.executeUpdate();
 
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateSponsor(Sponsor sponsor) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_SPONSOR);
 			statement.setString(1, sponsor.getSponsorName());
 			statement.setString(2, sponsor.getContactName());
 			statement.setString(3, sponsor.getContactPhone());
 			statement.setString(4, sponsor.getContactMail());
 			statement.setString(5, sponsor.getDescription());
 			statement.setInt(6, sponsor.getSponsorNum());
 			statement.executeUpdate();
 
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static List<Payment> getGroupPayments(int schoolNum, int activityYear) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		List<Payment> payments = new ArrayList<Payment>();
 		try {
 			statement = connection.prepareStatement(GET_GROUP_PAYMENTS);
 			statement.setInt(1, schoolNum);
 			statement.setInt(2, activityYear);
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				payments.add(new Payment(rs.getInt("paymentNum"), rs
 						.getInt("schoolNum"), rs.getInt("groupNum"), rs
 						.getInt("bindingYear"), rs.getInt("sponsorNum"), rs
 						.getDate("receiveDate"), rs.getDate("obligationDate"),
 						rs.getDouble("amount"), rs.getString("paymentContact"),
 						rs.getString("phone"), rs.getString("address"), rs
 								.getString("comments")));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return payments;
 	}
 
 	public static void deleteGroupPayment(int schoolNum, int activityYear,
 			int paymentNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_GROUP_PAYMENT);
 			statement.setInt(1, schoolNum);
 			statement.setInt(2, activityYear);
 			statement.setInt(3, paymentNum);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteGroupPayments(String schoolNum, String activityYear) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_GROUP_PAYMENTS);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(activityYear));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void insertGroupPayment(int schoolNum, int bindingYear,
 			Payment p) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_GROUP_PAYMENT);
 			statement.setInt(1, p.getPaymentNum());
 			statement.setInt(2, schoolNum);
 			statement.setInt(3, bindingYear);
 			statement.setInt(4, p.getSponsorNum());
 			statement.setDate(5, p.getReceiveDate());
 			statement.setDate(6, p.getObligationDate());
 			statement.setDouble(7, p.getAmount());
 			statement.setString(8, p.getPaymentContact());
 			statement.setString(9, p.getPhone());
 			statement.setString(10, p.getAddress());
 			statement.setString(11, p.getComments());
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateGroupPayment(int schoolNum, int bindingYear,
 			Payment p) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_GROUP_PAYMENT);
 			statement.setInt(1, p.getSponsorNum());
 			statement.setDate(2, p.getReceiveDate());
 			statement.setDate(3, p.getObligationDate());
 			statement.setDouble(4, p.getAmount());
 			statement.setString(5, p.getPaymentContact());
 			statement.setString(6, p.getPhone());
 			statement.setString(7, p.getAddress());
 			statement.setString(8, p.getComments());
 			statement.setInt(9, p.getPaymentNum());
 			statement.setInt(10, schoolNum);
 			statement.setInt(11, bindingYear);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static Instructor getInstructorByNum(String instructorNum) {
 		if (instructorNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		Integer insNum = Integer.parseInt(instructorNum);
 		PreparedStatement statement;
 		Instructor instructor = null;
 		try {
 			statement = connection.prepareStatement(GET_INSTRUCTOR_BY_NUM);
 			statement.setInt(1, insNum);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				String instructorName = rs.getString("instructorName");
 				String address = rs.getString("address");
 				String phone = rs.getString("phone");
 				String email = rs.getString("email");
 				String occupation = rs.getString("occupation");
 				Date birthdate = rs.getDate("birthdate");
 				String education = rs.getString("education");
 				String instructingExp = rs.getString("instructingExp");
 				String professionalExp = rs.getString("professionalExp");
 				String hobbies = rs.getString("hobbies");
 				String howCame = rs.getString("howCame");
 				String volunteerExp = rs.getString("volunteerExp");
 				String expectations = rs.getString("expectations");
 				String skills = rs.getString("skills");
 				String impressions = rs.getString("impressions");
 				if (rs.getInt("businessInsNum") != 0) {
 					instructor = new Instructor(insNum, instructorName,
 							address, phone, email, occupation, birthdate,
 							education, instructingExp, professionalExp,
 							hobbies, howCame, volunteerExp, expectations,
 							skills, impressions, new Company(
 									rs.getInt("companyNum"),
 									rs.getString("companyNum")),
 							InstructorTypes.BUSINESS);
 				} else if (rs.getInt("studentNum") != 0) {
 					instructor = new Instructor(insNum, instructorName,
 							address, phone, email, occupation, birthdate,
 							education, instructingExp, professionalExp,
 							hobbies, howCame, volunteerExp, expectations,
 							skills, impressions, rs.getInt("institutionNum"),
 							rs.getString("academicYear"),
 							InstructorTypes.STUDNET);
 				} else {
 					instructor = new Instructor(insNum, instructorName,
 							address, phone, email, occupation, birthdate,
 							education, instructingExp, professionalExp,
 							hobbies, howCame, volunteerExp, expectations,
 							skills, impressions, InstructorTypes.TEACHER);
 				}
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return instructor;
 	}
 
 	public static List<Group> getInstructorGroupsList(Integer instructorNum) {
 		if (instructorNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		City city;
 		School school;
 		Step step;
 		GroupType type;
 		Program program;
 		Group group;
 		List<Group> groups = new ArrayList<Group>();
 		try {
 			statement = connection.prepareStatement(GET_INSTRUCTOR_GROUPS_LIST);
 			statement.setInt(1, instructorNum);
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				city = new City(rs.getInt("cityNum"), rs.getString("cityName"));
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolName"), city);
 				step = new Step(rs.getInt("stepNum"), rs.getString("stepName"));
 				type = new GroupType(rs.getInt("groupTypeNum"),
 						rs.getString("groupTypeName"));
 				program = new Program(rs.getInt("programNum"),
 						rs.getString("programName"));
 				group = new Group(rs.getInt("groupNum"),
 						rs.getString("groupName"), rs.getInt("activityYear"),
 						rs.getInt("meetingDay"), rs.getTime("meetingTime"),
 						school, step, type, program);
 				groups.add(group);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return groups;
 	}
 
 	public static void deleteBuisnessInstructor(int instructorNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_BUISNESS_INSTRUCTOR);
 			statement.setInt(1, instructorNum);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteStudentInstructor(int instructorNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_STUDENT_INSTRUCTOR);
 			statement.setInt(1, instructorNum);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateInstructor(int instructorNum,
 			String instructorName, String address, String phone, String email,
 			String occupation, Date birthdate, String education,
 			String instructingExp, String professionalExp, String hobbies,
 			String howCame, String volunteerExp, String expectations,
 			String skills, String impressions, boolean isTeacher) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_INSTRUCTOR);
 			statement.setString(1, instructorName);
 			statement.setString(2, address);
 			statement.setString(3, phone);
 			statement.setString(4, email);
 			statement.setString(5, occupation);
 			statement.setDate(6, birthdate);
 			statement.setString(7, education);
 			statement.setString(8, instructingExp);
 			statement.setString(9, professionalExp);
 			statement.setString(10, hobbies);
 			statement.setString(11, howCame);
 			statement.setString(12, volunteerExp);
 			statement.setString(13, expectations);
 			statement.setString(14, skills);
 			statement.setString(15, impressions);
 			statement.setInt(16, instructorNum);
 			statement.executeUpdate();
 			if (isTeacher) {
 				deleteBuisnessInstructor(instructorNum);
 				deleteStudentInstructor(instructorNum);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateInstructor(int instructorNum,
 			String instructorName, String address, String phone, String email,
 			String occupation, Date birthdate, String education,
 			String instructingExp, String professionalExp, String hobbies,
 			String howCame, String volunteerExp, String expectations,
 			String skills, String impressions, int companyNum) {
 		updateInstructor(instructorNum, instructorName, address, phone, email,
 				occupation, birthdate, education, instructingExp,
 				professionalExp, hobbies, howCame, volunteerExp, expectations,
 				skills, impressions, false);
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection
 					.prepareStatement(REPLACE_BUSINESS_INSTRUCTOR);
 			statement.setInt(1, instructorNum);
 			statement.setInt(2, companyNum);
 			statement.executeUpdate();
 			deleteStudentInstructor(instructorNum);
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateInstructor(int instructorNum,
 			String instructorName, String address, String phone, String email,
 			String occupation, Date birthdate, String education,
 			String instructingExp, String professionalExp, String hobbies,
 			String howCame, String volunteerExp, String expectations,
 			String skills, String impressions, int institutionNum,
 			String academicYear) {
 		updateInstructor(instructorNum, instructorName, address, phone, email,
 				occupation, birthdate, education, instructingExp,
 				professionalExp, hobbies, howCame, volunteerExp, expectations,
 				skills, impressions, false);
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(REPLACE_STUDENT_INSTRUCTOR);
 			statement.setInt(1, instructorNum);
 			statement.setInt(2, institutionNum);
 			statement.setString(3, academicYear);
 			statement.executeUpdate();
 			deleteBuisnessInstructor(instructorNum);
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static int insertInstructor(String instructorName, String address,
 			String phone, String email, String occupation, Date birthdate,
 			String education, String instructingExp, String professionalExp,
 			String hobbies, String howCame, String volunteerExp,
 			String expectations, String skills, String impressions) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		int instructorNum = -1;
 		try {
 			statement = connection.prepareStatement(INSERT_INSTRUCTOR,
 					PreparedStatement.RETURN_GENERATED_KEYS);
 			statement.setString(1, instructorName);
 			statement.setString(2, address);
 			statement.setString(3, phone);
 			statement.setString(4, email);
 			statement.setString(5, occupation);
 			statement.setDate(6, birthdate);
 			statement.setString(7, education);
 			statement.setString(8, instructingExp);
 			statement.setString(9, professionalExp);
 			statement.setString(10, hobbies);
 			statement.setString(11, howCame);
 			statement.setString(12, volunteerExp);
 			statement.setString(13, expectations);
 			statement.setString(14, skills);
 			statement.setString(15, impressions);
 			statement.executeUpdate();
 			ResultSet rs = statement.getGeneratedKeys();
 			if (rs.next()) {
 				instructorNum = rs.getInt("GENERATED_KEY");
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return instructorNum;
 	}
 
 	public static int insertInstructor(String instructorName, String address,
 			String phone, String email, String occupation, Date birthdate,
 			String education, String instructingExp, String professionalExp,
 			String hobbies, String howCame, String volunteerExp,
 			String expectations, String skills, String impressions,
 			int companyNum) {
 		int instructorNum = insertInstructor(instructorName, address, phone,
 				email, occupation, birthdate, education, instructingExp,
 				professionalExp, hobbies, howCame, volunteerExp, expectations,
 				skills, impressions);
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection
 					.prepareStatement(REPLACE_BUSINESS_INSTRUCTOR);
 			statement.setInt(1, instructorNum);
 			statement.setInt(2, companyNum);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return instructorNum;
 	}
 
 	public static int insertInstructor(String instructorName, String address,
 			String phone, String email, String occupation, Date birthdate,
 			String education, String instructingExp, String professionalExp,
 			String hobbies, String howCame, String volunteerExp,
 			String expectations, String skills, String impressions,
 			int institutionNum, String academicYear) {
 		int instructorNum = insertInstructor(instructorName, address, phone,
 				email, occupation, birthdate, education, instructingExp,
 				professionalExp, hobbies, howCame, volunteerExp, expectations,
 				skills, impressions);
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(REPLACE_STUDENT_INSTRUCTOR);
 			statement.setInt(1, instructorNum);
 			statement.setInt(2, institutionNum);
 			statement.setString(3, academicYear);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return instructorNum;
 	}
 
 	public static void insertManufacturer(Manufacturer manufacturer) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_MANUFACTURER);
 			statement.setString(1, manufacturer.getManuName());
 			statement.setString(2, manufacturer.getAddress());
 			statement.setString(3, manufacturer.getContactName());
 			statement.setString(4, manufacturer.getContactPhone());
 			statement.setString(5, manufacturer.getContactMail());
 			statement.executeUpdate();
 
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateManufacturer(Manufacturer manufacturer) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_MANUFACTURER);
 			statement.setString(1, manufacturer.getManuName());
 			statement.setString(2, manufacturer.getAddress());
 			statement.setString(3, manufacturer.getContactName());
 			statement.setString(4, manufacturer.getContactPhone());
 			statement.setString(5, manufacturer.getContactMail());
 			statement.setInt(6, manufacturer.getManuNum());
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static List<Product> getManufacturerProducts(String manuNum) {
 		if (manuNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Group group;
 		School school;
 		Program program;
 		Manufacturer manufacturer;
 		ProductCategory category;
 		Product product;
 
 		List<Product> products = new ArrayList<Product>();
 		try {
 			statement = connection.prepareStatement(GET_MANUFACTURER_PRODUCTS);
 			statement.setInt(1, Integer.parseInt(manuNum));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				school = new School(rs.getInt("schoolNum"),
 						rs.getString("schoolName"));
 				program = new Program(rs.getInt("programNum"),
 						rs.getString("programName"));
 				group = new Group(school, rs.getInt("groupNum"),
 						rs.getString("groupName"), rs.getInt("activityYear"),
 						program);
 				manufacturer = new Manufacturer(Integer.parseInt(manuNum));
 				category = new ProductCategory(rs.getInt("catId"));
 				product = new Product(rs.getInt("productNum"), group,
 						rs.getString("productName"), manufacturer, category);
 				products.add(product);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return products;
 	}
 
 	public static Binding getBinding(String schoolNum, int activityYear) {
 		if (schoolNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Binding bind = null;
 		try {
 			statement = connection.prepareStatement(GET_BINDING_BY_SCHOOL);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, activityYear);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				bind = new Binding(rs.getInt("bindingYear"), new School(
 						rs.getInt("schoolNum")), rs.getBoolean("gotContract"),
 						rs.getDate("gotContractDate"),
 						rs.getBoolean("gotRegistration"),
 						rs.getDate("registrationDate"),
 						rs.getBoolean("sentMaterials"),
 						rs.getDate("materialsSentDate"),
 						rs.getBoolean("isGotMaterials"),
 						rs.getDate("materialsGotDate"),
 						rs.getString("gotMaterialsContact"),
 						rs.getString("materialsGotPhone"));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return bind;
 	}
 
 	public static void insertNewGroupBinding(String schoolNum,
 			String activityNum) {
 		if (schoolNum == null || activityNum == null)
 			return;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_NEW_GROUP_BINDING);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(activityNum));
 
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateNewGroupBinding(String schoolNum,
 			String activityNum) {
 		if (schoolNum == null || activityNum == null)
 			return;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_NEW_GROUP_BINDING);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(activityNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateBinding(String schoolNum, String activityNum,
 			Binding bind) {
 		if (bind == null)
 			return;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_BINDING);
 			statement.setBoolean(1, bind.isGotContract());
 
 			if (bind.getRegistrationDate() != null)
 				statement.setDate(2, bind.setFormattedDate(bind
 						.getRegistrationDate().toString()));
 			else
 				statement.setNull(2, Types.DATE);
 
 			statement.setBoolean(3, bind.isSentMaterials());
 
 			if (bind.getMaterialsSentDate() != null)
 				statement.setDate(4, bind.setFormattedDate(bind
 						.getMaterialsSentDate().toString()));
 			else
 				statement.setNull(4, Types.DATE);
 
 			statement.setBoolean(5, bind.isGotMaterials());
 
 			if (bind.getMaterialsGotDate() != null)
 				statement.setDate(6, bind.setFormattedDate(bind
 						.getMaterialsGotDate().toString()));
 			else
 				statement.setNull(6, Types.DATE);
 
 			statement.setString(7, bind.getGotMaterialsContact());
 			statement.setString(8, bind.getGotMaterialsPhone());
 
 			if (bind.getGotContractDate() != null)
 				statement.setDate(9, bind.setFormattedDate(bind
 						.getGotContractDate().toString()));
 			else
 				statement.setNull(9, Types.DATE);
 
 			statement.setBoolean(10, bind.isGotRegistration());
 			statement.setInt(11, Integer.parseInt(schoolNum));
 			statement.setInt(12, Integer.parseInt(activityNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void insertGroup(String groupName, int activityYear,
 			Integer meetingDay, String meetingTime, int schoolNum,
 			Integer stepNum, Integer groupTypeNum, Integer programNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_GROUP);
 			statement.setString(1, groupName);
 			statement.setInt(2, activityYear);
 			if (meetingDay != null) {
 				statement.setInt(3, meetingDay);
 			} else {
 				statement.setNull(3, Types.INTEGER);
 			}
 			statement.setString(4, meetingTime);
 			statement.setInt(5, schoolNum);
 			if (stepNum != null) {
 				statement.setInt(6, stepNum);
 			} else {
 				statement.setNull(6, Types.INTEGER);
 			}
 			if (groupTypeNum != null) {
 				statement.setInt(7, groupTypeNum);
 			} else {
 				statement.setNull(7, Types.INTEGER);
 			}
 			if (programNum != null) {
 				statement.setInt(8, programNum);
 			} else {
 				statement.setNull(8, Types.INTEGER);
 			}
 			statement.executeUpdate();
 
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteBinding(String schoolNum, String bindingYear) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_BINDING);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(bindingYear));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateGroup(String groupNum, String groupName,
 			String activityYear, String meetingDay, String meetingTime,
 			String schoolNum, String stepNum, String groupTypeNum,
 			String programNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_GROUP);
 			statement.setString(1, groupName);
 			statement.setInt(2, Integer.parseInt(activityYear));
 			if (meetingDay != null) {
 				statement.setInt(3, Integer.parseInt(meetingDay));
 			} else {
 				statement.setNull(3, Types.INTEGER);
 			}
 			statement.setString(4, meetingTime);
 			if (stepNum != null) {
 				statement.setString(5, stepNum);
 			} else {
 				statement.setNull(5, Types.INTEGER);
 			}
 			if (groupTypeNum != null) {
 				statement.setInt(6, Integer.parseInt(groupTypeNum));
 			} else {
 				statement.setNull(6, Types.INTEGER);
 			}
 			if (programNum != null) {
 				statement.setInt(7, Integer.parseInt(programNum));
 			} else {
 				statement.setNull(7, Types.INTEGER);
 			}
 			statement.setString(8, schoolNum);
 			statement.setInt(9, Integer.parseInt(groupNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteGroupInstructors(String schoolNum, String groupNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_GROUP_INSTRUCTORS);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(groupNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteGroup(String schoolNum, String groupNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_GROUP);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(groupNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static boolean checkSchoolandActivityInGroups(String schoolNum,
 			String activityYear) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection
 					.prepareStatement(CHECK_SCHOOL_AND_ACTIVITY_IN_GROUPS);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(activityYear));
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				return true;
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return false;
 	}
 
 	public static String getInstructorType(String instructorNum) {
 		if (instructorNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		String instructorType = null;
 		try {
 			statement = connection.prepareStatement(GET_INSTRUCTOR_TYPE);
 			statement.setInt(1, Integer.parseInt(instructorNum));
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				instructorType = rs.getString("insType");
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return instructorType;
 	}
 
 	public static void insertGroupInstructor(String schoolNum, String groupNum,
 			String instructorNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_GROUP_INSTRUCTOR);
 
 			statement.setInt(1, Integer.parseInt(groupNum));
 			statement.setInt(2, Integer.parseInt(schoolNum));
 			statement.setInt(3, Integer.parseInt(instructorNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteGroupInstructor(String schoolNum, String groupNum,
 			String instructorNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_GROUP_INSTRUCTOR);
 			statement.setInt(1, Integer.parseInt(groupNum));
 			statement.setInt(2, Integer.parseInt(schoolNum));
 			statement.setInt(3, Integer.parseInt(instructorNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static Company getCompanyByNum(String companyNum) {
 		if (companyNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		Company company = null;
 		try {
 			statement = connection.prepareStatement(GET_COMPANY_BY_NUM);
 			statement.setInt(1, Integer.parseInt(companyNum));
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				company = new Company(rs.getInt("companyNum"),
 						rs.getString("companyName"));
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return company;
 	}
 
 	public static int getEmployeeAreaNum(String username) {
 		if (username == null)
 			return -1;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		int areaNum = -1;
 		try {
 			statement = connection.prepareStatement(GET_EMPLOYEE_AREA);
 			statement.setString(1, username);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				areaNum = rs.getInt("areaNum");
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return areaNum;
 	}
 
 	public static boolean isAdmin(String username) {
 		if (username == null)
 			return false;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		boolean admin = false;
 		try {
 			statement = connection.prepareStatement(GET_EMPLOYEE_ROLENAME);
 			statement.setString(1, username);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				if (rs.getString("rolename").equals("admin"))
 					admin = true;
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return admin;
 	}
 
 	public static void insertNewArea(String areaName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_NEW_AREA);
 			statement.setString(1, areaName);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateAreaName(String areaNum, String areaName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_AREA_NAME);
 			statement.setString(1, areaName);
 			statement.setInt(2, Integer.parseInt(areaNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteArea(String areaNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_AREA);
 			statement.setInt(1, Integer.parseInt(areaNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void insertNewCity(int areaNum, String cityName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_NEW_CITY);
 			statement.setString(1, cityName);
 			statement.setInt(2, areaNum);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateCityName(String cityNum, String cityName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_CITY_NAME);
 			statement.setString(1, cityName);
 			statement.setInt(2, Integer.parseInt(cityNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteCity(String cityNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_CITY);
 			statement.setInt(1, Integer.parseInt(cityNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void insertNewSchoolNetwork(String netName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_NEW_SCHOOL_NETWORK);
 			statement.setString(1, netName);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateSchoolNetworkName(String netNum, String netName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_SCHOOL_NETWORK_NAME);
 			statement.setString(1, netName);
 			statement.setInt(2, Integer.parseInt(netNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteSchoolNetwork(String netNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_SCHOOL_NETWORK);
 			statement.setInt(1, Integer.parseInt(netNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void insertNewSchoolType(String typeName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_NEW_SCHOOL_TYPE);
 			statement.setString(1, typeName);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateSchoolTypeName(String typeNum, String typeName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_SCHOOL_TYPE_NAME);
 			statement.setString(1, typeName);
 			statement.setInt(2, Integer.parseInt(typeNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteSchoolType(String typeNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_SCHOOL_TYPE);
 			statement.setInt(1, Integer.parseInt(typeNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void insertNewGroupType(String typeName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_NEW_GROUP_TYPE);
 			statement.setString(1, typeName);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateGroupTypeName(String groupTypeNum,
 			String groupTypeName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_GROUP_TYPE_NAME);
 			statement.setString(1, groupTypeName);
 			statement.setInt(2, Integer.parseInt(groupTypeNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteGroupType(String groupTypeNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_GROUP_TYPE);
 			statement.setInt(1, Integer.parseInt(groupTypeNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void insertNewProgram(String programName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_NEW_PROGRAM);
 			statement.setString(1, programName);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateProgramName(String programNum, String programName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_PROGRAM_NAME);
 			statement.setString(1, programName);
 			statement.setInt(2, Integer.parseInt(programNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteProgram(String programNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_PROGRAM);
 			statement.setInt(1, Integer.parseInt(programNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void insertNewStep(String stepName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_NEW_STEP);
 			statement.setString(1, stepName);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateStepName(String stepNum, String stepName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_STEP_NAME);
 			statement.setString(1, stepName);
 			statement.setInt(2, Integer.parseInt(stepNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteStep(String stepNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_STEP);
 			statement.setInt(1, Integer.parseInt(stepNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void insertNewCategory(String catName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection
 					.prepareStatement(INSERT_NEW_PRODUCT_CATEGORY);
 			statement.setString(1, catName);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void updateProductCategoryName(String catId, String catName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection
 					.prepareStatement(UPDATE_PRODUCT_CATEGORY_NAME);
 			statement.setString(1, catName);
 			statement.setInt(2, Integer.parseInt(catId));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteProductCategory(String catId) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_PRODUCT_CATEGORY);
 			statement.setInt(1, Integer.parseInt(catId));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static int insertNewCompany(String companyName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		int companyNum = -1;
 		try {
 			statement = DBConnection.getConnection()
 					.prepareStatement(INSERT_NEW_COMPANY,
 							PreparedStatement.RETURN_GENERATED_KEYS);
 			statement.setString(1, companyName);
 			statement.executeUpdate();
 			ResultSet rs = statement.getGeneratedKeys();
 			if (rs.next()) {
 				companyNum = rs.getInt("GENERATED_KEY");
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return companyNum;
 	}
 
 	public static void updateCompanyName(String companyNum, String companyName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_COMPANY_NAME);
 			statement.setString(1, companyName);
 			statement.setInt(2, Integer.parseInt(companyNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteCompany(String companyNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_COMPANY);
 			statement.setInt(1, Integer.parseInt(companyNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static int insertNewInstitution(String institutionName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		int newInstitution = -1;
 		try {
 			statement = connection.prepareStatement(INSERT_NEW_INSTITUTION,
 					PreparedStatement.RETURN_GENERATED_KEYS);
 			statement.setString(1, institutionName);
 			statement.executeUpdate();
 			ResultSet rs = statement.getGeneratedKeys();
 			if (rs.next()) {
 				newInstitution = rs.getInt("GENERATED_KEY");
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return newInstitution;
 	}
 
 	public static void updateInstitutionName(String institutionNum,
 			String institutionName) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(UPDATE_INSTITUTION_NAME);
 			statement.setString(1, institutionName);
 			statement.setInt(2, Integer.parseInt(institutionNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteInstitution(String institutionNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_INSTITUTION);
 			statement.setInt(1, Integer.parseInt(institutionNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void insertUpdate(String description, String link,
 			String username) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_UPDATE);
 			Calendar calendar = Calendar.getInstance();
 			Timestamp currentTimestamp = new java.sql.Timestamp(calendar
 					.getTime().getTime());
 			statement.setTimestamp(1, currentTimestamp);
 			statement.setString(2, description);
 			statement.setString(3, link);
 			statement.setString(4, username);
 			statement.executeUpdate();
 
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static List<Update> getUpdatesList(int areaNum, int dateRange,
 			String username) {
		Connection connection = DBConnection.getConnection();
 
 		PreparedStatement statement = null;
 		Update update = null;
 		List<Update> updates = new ArrayList<Update>();
 
 		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
 		long DAY_IN_MS = 1000 * 60 * 60 * 24;
 		Date date = null;
 		String strDate = "";
 		if (dateRange == 1)
 			date = new Date(System.currentTimeMillis() - (7 * DAY_IN_MS));
 		else if (dateRange == 2)
 			date = new Date(System.currentTimeMillis() - (30 * DAY_IN_MS));
 		else if (dateRange == 3)
 			date = new Date(System.currentTimeMillis() - (365 * DAY_IN_MS));
 
 		if (dateRange != 0)
 			strDate = "and u.up_date > '" + sdfDate.format(date) + "'";
 
 		try {
 			if (areaNum == 0) {
				statement = connection
						.prepareStatement(GET_UPDATES_LIST
								+ " WHERE u.updateNum not in (select ur.updateNum from updates_removed ur where ur.username = ?) "
								+ strDate);
 				statement.setString(1, username);
 			} else {
				statement = connection
						.prepareStatement(GET_UPDATES_LIST
								+ " WHERE e.areaNum=? and u.updateNum not in (select ur.updateNum from updates_removed ur where ur.username = ?) "
								+ strDate);
 				statement.setInt(1, areaNum);
 				statement.setString(2, username);
 			}
 
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				update = new Update(rs.getInt("updateNum"),
 						rs.getDate("up_date"), rs.getString("description"),
 						rs.getString("link"), new Employee(
 								rs.getString("username"), rs.getString("name"),
 								new Area(rs.getInt("areaNum"),
 										rs.getString("areaName"))));
 				updates.add(update);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
		} finally {
			DBConnection.closeConnection(connection);
 		}

 		return updates;
 	}
 
 	public static void insertUpdate_Removed(int updateNum, String username) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_UPDATE_REMOVED);
 			statement.setInt(1, updateNum);
 			statement.setString(2, username);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static List<GroupStatus> getGroupStatusesList(String schoolNum,
 			String groupNum) {
 		if (schoolNum == null || groupNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		GroupStatus status;
 		List<GroupStatus> statuses = new ArrayList<GroupStatus>();
 		try {
 			statement = connection.prepareStatement(GET_GROUP_STATUSES_LIST);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(groupNum));
 			ResultSet rs = statement.executeQuery();
 			while (rs.next()) {
 				status = new GroupStatus(rs.getInt("statusNum"),
 						rs.getInt("schoolNum"), rs.getInt("groupNum"),
 						rs.getDate("statusDate"), rs.getString("details"));
 				statuses.add(status);
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return statuses;
 	}
 
 	public static void insertGroupStatus(int statusNum, Date statusDate,
 			String details, String groupNum, String schoolNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(INSERT_GROUP_STATUS);
 			statement.setInt(1, statusNum);
 			statement.setDate(2, statusDate);
 			statement.setString(3, details);
 			statement.setInt(4, Integer.parseInt(groupNum));
 			statement.setInt(5, Integer.parseInt(schoolNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteGroupStatus(String schoolNum, String groupNum,
 			int statusNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_GROUP_STATUS);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(groupNum));
 			statement.setInt(3, statusNum);
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteGroupStatuses(String schoolNum, String groupNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(DELETE_GROUP_STATUSES);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(groupNum));
 			statement.executeUpdate();
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 	}
 
 	public static void deleteGroupAndRelations(String schoolNum,
 			String groupNum, String activityYear) {
 		deleteProduct(schoolNum, groupNum);
 		deleteGroupInstructors(schoolNum, groupNum);
 		deleteGroupMembers(schoolNum, groupNum);
 		deleteGroupStatuses(schoolNum, groupNum);
 		deleteGroup(schoolNum, groupNum);
 
 		if (!checkSchoolandActivityInGroups(schoolNum, activityYear)) {
 			deleteGroupPayments(schoolNum, activityYear);
 			deleteBinding(schoolNum, activityYear);
 		}
 	}
 
 	public static String reportSchoolsByMarketingStatus() {
 		return REPORT_SCHOOLS_BY_MARKETING_STATUS;
 	}
 
 	public static String reportSchoolsByNet() {
 		return REPORT_SCHOOLS_BY_NET;
 	}
 
 	public static String reportSchoolsByType() {
 		return REPORT_SCHOOLS_BY_TYPE;
 	}
 
 	public static String reportGroupsByYear() {
 		return REPORT_GROUPS_LIST_BY_YEAR;
 	}
 
 	public static String reportGroupMembersList() {
 		return REPORT_GROUP_MEMBERS_LIST;
 	}
 
 	public static String reportGroupStatusList() {
 		return REPORT_GROUP_STATUS_LIST;
 	}
 
 	public static ResultSet reportGroupDetails(String schoolNum, String groupNum) {
 		if (schoolNum == null || groupNum == null)
 			return null;
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		try {
 			statement = connection.prepareStatement(REPORT_GROUP_DETAILS);
 			statement.setInt(1, Integer.parseInt(schoolNum));
 			statement.setInt(2, Integer.parseInt(groupNum));
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				return rs;
 			}
 		} catch (Exception e) {
 			System.err.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			DBConnection.closeConnection(connection);
 		}
 		return null;
 	}
 
 	public static ResultSet reportSponsorsDetails(String sponsorNum) {
 		Connection connection = DBConnection.getConnection();
 		PreparedStatement statement;
 		if (sponsorNum == null || sponsorNum == "-1") {
 			try {
 				statement = connection
 						.prepareStatement(REPORT_SPONSORS_DETAILS);
 				ResultSet rs = statement.executeQuery();
 				if (rs.next()) {
 					return rs;
 				}
 			} catch (Exception e) {
 				System.err.println("Exception: " + e.getMessage());
 				e.printStackTrace();
 			} finally {
 				DBConnection.closeConnection(connection);
 			}
 		} else {
 			try {
 				statement = connection.prepareStatement(REPORT_SPONSORS_DETAILS
 						+ "WHERE sponsorNum = ?");
 				statement.setInt(1, Integer.parseInt(sponsorNum));
 				ResultSet rs = statement.executeQuery();
 				if (rs.next()) {
 					return rs;
 				}
 			} catch (Exception e) {
 				System.err.println("Exception: " + e.getMessage());
 				e.printStackTrace();
 			} finally {
 				DBConnection.closeConnection(connection);
 			}
 		}
 		return null;
 	}
 
 	public static String reportSponsorPayments() {
 		return REPORT_SPONSOR_PAYMENTS;
 	}
 
 	public static String reportSponsorsPayments() {
 		return REPORT_SPONSORS_PAYMENTS;
 	}
 
 	public static String reportProductsByManufacturer() {
 		return REPORT_PRODUCTS_BY_MANUFACTURER;
 	}
 
 	public static String reportProductsByCategory() {
 		return REPORT_PRODUCTS_BY_CATEGORY;
 	}
 
 	public static String reportBindingByYear() {
 		return REPORT_BINDING_BY_YEAR;
 	}
 
 	public static String reportInstructorsInGroups() {
 		return REPORT_INSTRUCTORS_IN_GROUPS;
 	}
 }
