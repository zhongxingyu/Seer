 package controllers;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import jxl.Workbook;
 import jxl.format.Alignment;
 import jxl.format.Colour;
 import jxl.format.UnderlineStyle;
 import jxl.write.Label;
 import jxl.write.WritableCellFormat;
 import jxl.write.WritableFont;
 import jxl.write.WritableSheet;
 import jxl.write.WritableWorkbook;
 import play.modules.morphia.Model.MorphiaQuery;
 import play.mvc.Controller;
 import utils.Utils;
 
 import com.mongodb.util.JSON;
 /** 休长假职工登记管理 */
 public class Furlough extends Controller {
 	public static final int PAGE_SIZE = 20;
 	/** 管理入口 .*/
 	public static void index(String keyword, String department, int page, int size) {
 		List<models.Department> divisions = Secure.getDepartments();
 		render(keyword, department, divisions);
 	}
 	
 	/** 导出excel. */
 	public static void report(String keyword, String department) {
 		int rows = 100000;
 		int page = 1;
 		List<models.Furlough> lists = null;
 		if (department == null || "".equals(department)) {
 			lists = findAll(keyword, page, rows);
 		} else {
 			models.Department dep = models.Department.findById(department);
 			if (dep != null) {
 				MorphiaQuery query = models.Furlough.find();
 				if (keyword != null && !"".equals(keyword))
 					query.filter("name", keyword);
 				query.filter("department", dep);
 				lists = query.limit(rows).offset(page * rows - rows).asList();
 			} else {
 				lists = findAll(keyword, page, rows);
 			}
 		}
 
 		String fileName = "休长假职工登记-" + Secure.getAdmin().department.name + ".xls";
 		File f = new File("./excels/" + fileName);
 		try {
 			if (f.exists())
 				f.createNewFile();
 			WritableWorkbook wbook = Workbook.createWorkbook(f);
 			WritableSheet ws = wbook.createSheet("休长假职工登记", 0);
 			
 			WritableFont wfont = new WritableFont(WritableFont.ARIAL, 16,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK);   
 			WritableCellFormat wcfFC = new WritableCellFormat(wfont); 
 			wcfFC.setAlignment(Alignment.CENTRE);
 			int index = 0;
 			Label label0_0 = new Label(0, index, "休长假职工登记", wcfFC);
 			ws.addCell(label0_0);
 			ws.mergeCells(0, index, 12, index); 
 			index++;
 			Label label0_1 = new Label(0, index, "单位");
 			ws.addCell(label0_1);
 			Label label1_1 = new Label(1, index, Secure.getAdmin().department.name);
 			ws.addCell(label1_1);
 			index++;
 			
 			Label label13_2 = new Label(0, index, "序号");
 			ws.addCell(label13_2);
 			Label label0_2 = new Label(1, index, "单位");
 			ws.addCell(label0_2);
 			Label label1_2 = new Label(2, index, "姓名");
 			ws.addCell(label1_2);
 			Label label2_2 = new Label(3, index, "性别");
 			ws.addCell(label2_2);
 			Label label3_2 = new Label(4, index, "民族");
 			ws.addCell(label3_2);
 			Label label4_2 = new Label(5, index, "出生年月");
 			ws.addCell(label4_2);
 			Label label5_2 = new Label(6, index, "爱人姓名");
 			ws.addCell(label5_2);
 			Label label6_2 = new Label(7, index, "民族");
 			ws.addCell(label6_2);
 			Label label7_2 = new Label(8, index, "出生年月");
 			ws.addCell(label7_2);
 			Label label8_2 = new Label(9, index, "工作单位");
 			ws.addCell(label8_2);
 			Label label9_2 = new Label(10, index, "节育措施");
 			ws.addCell(label9_2);
 			Label label10_2 = new Label(11, index, "现住址");
 			ws.addCell(label10_2);
 			Label label11_2 = new Label(12, index, "联系电话");
 			ws.addCell(label11_2);
 			Label label12_2 = new Label(13, index, "备注");
 			ws.addCell(label12_2);
 			index ++;
 			
 			for (int i = 0; i < lists.size(); i ++) {
 				models.Furlough data = lists.get(i);
 				jxl.write.Number field3_0 = new jxl.write.Number(0, index, i + 1);
 		        ws.addCell(field3_0);
 		        Label field3_1 = new Label(1, index, data.department.name);
 		        ws.addCell(field3_1);
 		        Label field3_2 = new Label(2, index, data.name);
 		        ws.addCell(field3_2);
 		        Label field3_3 = new Label(3, index, data.gender);
 		        ws.addCell(field3_3);
 		        Label field3_4 = new Label(4, index, data.nation);
 		        ws.addCell(field3_4);
 		        Label field3_5 = new Label(5, index, utils.Utils.formatDate(data.birth));
 		        ws.addCell(field3_5);
 		        Label field3_6 = new Label(6, index, data.spouse);
 		        ws.addCell(field3_6);
 		        Label field3_7 = new Label(7, index, data.spouseNation);
 		        ws.addCell(field3_7);
 		        Label field3_8 = new Label(8, index, utils.Utils.formatDate(data.spouseBirth));
 		        ws.addCell(field3_8);
 		        Label field3_9 = new Label(9, index, data.spouseWork);
 		        ws.addCell(field3_9);
 		        Label field3_10 = new Label(10, index, data.measure);
 		        ws.addCell(field3_10);
 		        Label field3_11 = new Label(11, index, data.live);
 		        ws.addCell(field3_11);
 		        Label field3_12 = new Label(12, index, data.tel);
 		        ws.addCell(field3_12);
 		        Label field3_13 = new Label(13, index, data.notes);
 		        ws.addCell(field3_13);
 		        index ++;
 	        }
 
 			wbook.write();
 			wbook.close();
 			renderBinary(f, fileName);
 		} catch (Exception exception) {
 			exception.printStackTrace();
 			error(exception);
 		}
 	}
 	
 	/** 获取数据列表. */
 	public static void list(String keyword, String department, int page, int rows) {
 		rows = rows != 0 ? rows : PAGE_SIZE;
 		List<models.Furlough> lists = null;
 		if (department == null || "".equals(department)) {
 			lists = findAll(keyword, page, rows);
 		} else {
 			models.Department dep = models.Department.findById(department);
 			if (dep != null) {
 				MorphiaQuery query = models.Furlough.find();
 				if (keyword != null && !"".equals(keyword))
 					query.filter("name", keyword);
 				query.filter("department", dep);
 				lists = query.limit(rows).offset(page * rows - rows).asList();
 			} else {
 				lists = findAll(keyword, page, rows);
 			}
 		}
 		List<Object> datas = new ArrayList<Object>();
 		Map<String, Object> result = new HashMap<String, Object>();
 		for (models.Furlough mode : lists) {
 			datas.add(mode.serialize());
 		}
 		result.put("page", page);
 		result.put("rows", datas);
 		result.put("total", models.Furlough.count());
 		result.put("size", rows);
 		renderJSON(result);
 	}
 	
 	/** 获取当前操作者的全部数据. */
 	private static List<models.Furlough> findAll(String keyword, int page, int rows) {
 		List<models.Department> departments = Secure.getDepartments();
 		List<models.Furlough> result = new ArrayList<models.Furlough>();
 		int size = 0, offset = page * rows - rows, end = offset + rows;
 		for (models.Department department : departments) {
 			MorphiaQuery query = models.Furlough.find("byDepartment", department);
 			if (keyword != null && !"".equals(keyword))
 				query.filter("name", keyword);
 			List<models.Furlough> data = query.asList();
 			if (size >= offset && size < end) {
 				for (models.Furlough flup : data) {
 					result.add(flup);
 					size ++;
 				}
 			} else {
 				size += data.size();
 			}
 			if (size >= end) {
 				break;
 			}
 		}
 		return result;
 	}
 	
 	/** 添加操作 */
 	public static void add(String name, String gender, String nation, String birth, 
 			String spouse, String spouseNation, String spouseBirth, String spouseWork,
 			String measure, String live, String tel, String department, String notes) {
 		Map<String, String> result = new HashMap<String, String>();
		if (department == null || "".equals(department)) {
			result.put("error", "长假记录添加失败，请选择该所属部门。<br>如果还未创建部门，请先创建部门后进行添加");
 			renderText(JSON.serialize(result));
 		}
 		models.Furlough newData = new models.Furlough();
 		Date newDate = new Date();
 		newData.name = name;
 		newData.gender = gender;
 		newData.nation = nation;
 		newData.birth = Utils.parseDate(birth);
 		newData.spouse = spouse;
 		newData.spouseNation = spouseNation;
 		newData.spouseBirth = Utils.parseDate(spouseBirth);
 		newData.spouseWork = spouseWork;
 		newData.measure = measure;
 		newData.live = live;
 		newData.nation = nation;
 		newData.tel = tel;
 		newData.notes = notes;
 		newData.createAt = newDate;
 		newData.modifyAt = newDate;
 		newData.department = models.Department.findById(department);
 		newData.save();
 		renderText(JSON.serialize(result));
 	}
 	
 	/** 删除操作 */
 	public static void del(String id) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		try {
 			models.Furlough.findById(id).delete();
 			result.put("success", true);
 		} catch (Exception exc) {
 			result.put("error", "数据库异常，可能其他人正在操作，请刷新后重试。");
 		}
 		renderText(JSON.serialize(result));
 	}
 	
 	/** 修改操作 */
 	public static void update(String id, String name, String gender, String nation, String birth, 
 			String spouse, String spouseNation, String spouseBirth, String spouseWork,
 			String measure, String live, String tel, String department, String notes) {
 		Map<String, Object> result = new HashMap<String, Object>();
 		if (department == null || "".equals(department)) {
			result.put("error", "长假记录添加失败，请选择该所属部门。<br>如果还未创建部门，请先创建部门后进行添加");
 			renderText(JSON.serialize(result));
 		}
 		try {
 			models.Furlough cur = models.Furlough.findById(id);
 			cur.name = name;
 			cur.gender = gender;
 			cur.nation = nation;
 			cur.birth = Utils.parseDate(birth);
 			cur.spouse = spouse;
 			cur.spouseNation = spouseNation;
 			cur.spouseBirth = Utils.parseDate(spouseBirth);
 			cur.spouseWork = spouseWork;
 			cur.measure = measure;
 			cur.live = live;
 			cur.nation = nation;
 			cur.tel = tel;
 			cur.notes = notes;
 			cur.modifyAt = new Date();
 			cur.department = models.Department.findById(department);
 			cur.save();
 			result.put("success", true);
 		} catch (Exception exc) {
 			exc.printStackTrace();
 			result.put("error", "数据库异常，可能其他人正在操作，请刷新后重试。");
 		}
 		renderText(JSON.serialize(result));
 	}
 }
