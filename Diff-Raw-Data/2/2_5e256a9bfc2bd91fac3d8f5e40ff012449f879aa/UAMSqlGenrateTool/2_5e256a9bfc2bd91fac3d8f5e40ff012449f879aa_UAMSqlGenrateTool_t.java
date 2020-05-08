 package org.txxfu.sqltool;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.digester.Digester;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.CharEncoding;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.core.io.DefaultResourceLoader;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.ResourceLoader;
 import org.xml.sax.SAXException;
 
 public class UAMSqlGenrateTool {
 
 	private static final String MENU_CLASS = "org.txxfu.sqltool.Menu";
	private static final String CONTEXT_PATH = "http://localtest:5050/yxcard-management-web";
 	private static ResourceLoader resourceLoader = new DefaultResourceLoader();
 
 	static Menu parseMenu() throws IOException, SAXException {
 		Resource resource = resourceLoader.getResource("YXCard-back.xml");
 
 		Digester digester = new Digester();
 		// digester.setValidating(true);
 		digester.addObjectCreate("menu", MENU_CLASS);
 		digester.addSetProperties("menu");
 
 		digester.addObjectCreate("menu/menu", MENU_CLASS);
 		digester.addSetProperties("menu/menu");
 		digester.addSetNext("menu/menu", "addSubMenu", MENU_CLASS);
 
 		digester.addObjectCreate("menu/menu/menu", MENU_CLASS);
 		digester.addSetProperties("menu/menu/menu");
 		digester.addSetNext("menu/menu/menu", "addSubMenu", MENU_CLASS);
 
 		return (Menu) digester.parse(resource.getInputStream());
 	}
 
 	static void printMenu(Menu menu) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(menu.toString() + "\n");
 		for (Menu subMenu : menu.getMenus()) {
 			sb.append("  " + subMenu.toString() + "\n");
 			for (Menu link : subMenu.getMenus()) {
 				sb.append("    ").append(link.toString()).append("\n");
 			}
 		}
 		System.out.println(sb.toString());
 	}
 
 	public static void main(String[] args) throws IOException, SAXException {
 		Long menuIdStart = 168260L;
 		Long privIdStart = 60670L;
 		int seqIncr = 20;
 
 		Menu menu = parseMenu();
 		menu.setSqlId(String.valueOf(menuIdStart));
 		menu.setPrivId(String.valueOf(privIdStart));
 		Long sqlId = menuIdStart + 1;
 		Long privId = privIdStart + 1;
 		for (Menu subMenu : menu.getMenus()) {
 			subMenu.setSqlId(String.valueOf(sqlId++));
 			subMenu.setPrivId(String.valueOf(privId++));
 			for (Menu link : subMenu.getMenus()) {
 				link.setSqlId(String.valueOf(sqlId++));
 				link.setPrivId(String.valueOf(privId++));
 			}
 		}
 		// printMenu(menu);
 
 		generateSeqSql(menuIdStart, seqIncr, sqlId);
 
 		generateSql(menu);
 
 		genrateDelSql(menuIdStart, privIdStart, sqlId, privId);
 
 		// printMenu(menu);
 
 	}
 
 	private static void genrateDelSql(Long menuIdStart, Long privIdStart,
 			Long sqlId, Long privId) throws IOException {
 		StringBuilder delSql = new StringBuilder("-- del sql \n");
 		delSql.append("DELETE FROM tb_adm_permission t WHERE t.id >= "
 				+ privIdStart + " AND t.id < " + privId + ";\n");
 		delSql.append("DELETE FROM tb_adm_menu t WHERE t.id >= " + menuIdStart
 				+ " AND t.id < " + sqlId + ";");
 		System.out.println(delSql);
 		FileUtils.writeStringToFile(new File("data/sql/del.sql"),
 				delSql.toString(), CharEncoding.UTF_8);
 	}
 
 	private static void generateSeqSql(Long menuIdStart, int seqIncr, Long sqlId)
 			throws IOException {
 		Long menuCount = sqlId - menuIdStart;
 		Long seqGap = menuCount + seqIncr;
 		StringBuilder seqSql = new StringBuilder("-- seq sql \n");
 		seqSql.append("-- total menu count " + menuCount + "\n");
 		seqSql.append("ALTER SEQUENCE SEQ_ADM_MENU INCREMENT BY " + seqGap
 				+ " ;\n");
 		seqSql.append("SELECT SEQ_ADM_MENU.NEXTVAL FROM DUAL;\n");
 		seqSql.append("ALTER SEQUENCE SEQ_ADM_MENU INCREMENT BY 1;\n");
 		seqSql.append("ALTER SEQUENCE SEQ_ADM_PERMISSION INCREMENT BY "
 				+ seqGap + " ;\n");
 		seqSql.append("SELECT SEQ_ADM_PERMISSION.NEXTVAL FROM DUAL;\n");
 		seqSql.append("ALTER SEQUENCE SEQ_ADM_PERMISSION INCREMENT BY 1;\n");
 		System.out.println(seqSql);
 		FileUtils.writeStringToFile(new File("data/sql/seq.sql"),
 				seqSql.toString(), CharEncoding.UTF_8);
 	}
 
 	static void generateSql(Menu menu) throws IOException {
 		StringBuilder menuSql = new StringBuilder("-- menu sql \n");
 		StringBuilder privSql = new StringBuilder("-- priv sql \n");
 		generateSql0(menu, menuSql, privSql, 0);
 
 		for (Menu m : menu.getMenus()) {
 			menuSql.append("-- " + m.getName() + " menu sql \n");
 			privSql.append("-- " + m.getName() + " priv sql \n");
 			generateSql0(m, menuSql, privSql, 1);
 			for (Menu link : m.getMenus()) {
 				generateSql0(link, menuSql, privSql, 2);
 			}
 		}
 		System.out.println(menuSql);
 		System.out.println(privSql);
 		FileUtils.writeStringToFile(new File("data/sql/menu.sql"),
 				menuSql.toString(), CharEncoding.UTF_8);
 		FileUtils.writeStringToFile(new File("data/sql/priv.sql"),
 				privSql.toString(), CharEncoding.UTF_8);
 	}
 
 	private static void generateSql0(Menu menu, StringBuilder menuSql,
 			StringBuilder privSql, int menuLevel) {
 		String tSqlId = menu.getSqlId();
 		String tPrivId = menu.getPrivId();
 		String tId = menu.getId();
 		String tName = menu.getName();
 		String tSort = StringUtils.defaultIfBlank(menu.getSort(), "0");
 		String tActionUrl = StringUtils
 				.defaultIfBlank(menu.getActionUrl(), "#");
 		String tActionType = StringUtils.defaultIfBlank(menu.getActionType(),
 				"M");
 		String tContextPath;
 		if (menuLevel == 2) {
 			tContextPath = CONTEXT_PATH;
 		} else {
 			tContextPath = StringUtils.EMPTY;
 		}
 		String tParentId = StringUtils.defaultIfBlank(null == menu
 				.getParentMenu() ? null : menu.getParentMenu().getSqlId(), "0");
 
 		generateMenuSql0(menuSql, tSqlId, tId, tName, tSort, tActionUrl,
 				tActionType, tContextPath, tParentId);
 
 		generatePrivSql0(privSql, tSqlId, tPrivId, tId, tName);
 	}
 
 	private static void generatePrivSql0(StringBuilder sql, String tSqlId,
 			String tPrivId, String tId, String tName) {
 		String priv1 = "insert into tb_adm_permission (ID, PERMISSION_KEY, DESCRIPTION, STATUS, MENU_ID, CREATOR, GMT_CREATE, GMT_MODIFIED, MEMO)\n";
 		// "values ('60632', 'PPC-NSC-OM-QUERY-P', '订单管理-订单查询', 'Y', '168232', 'SNDA-wangjinhua', '2011-12-28 14:50:33.000000', '2011-12-28 14:50:40.000000', '');\n\n";
 		String priv2 = "values ('"
 				+ tPrivId
 				+ "', '"
 				+ tId
 				+ "-P"
 				+ "', '"
 				+ tName
 				+ "', 'Y', '"
 				+ tSqlId
 				+ "', 'SNDA-wangjinhua', '2011-12-28 14:50:33.000000', '2011-12-28 14:50:40.000000', '');\n\n";
 		sql.append(priv1);
 		sql.append(priv2);
 	}
 
 	private static void generateMenuSql0(StringBuilder sql, String tSqlId,
 			String tId, String tName, String tSort, String tActionUrl,
 			String tActionType, String tContextPath, String tParentId) {
 		String menu1 = "insert into tb_adm_menu (ID, MENU_CODE, MENU_NAME, ACTION_URL, IMG_URL, SORT, DESCRIPTION, STATUS, ACTION_TYPE, PARENT_ID, CREATOR, GMT_CREATE, GMT_MODIFIED, MEMO)\n";
 		// "values ('168231', 'PPC-NSC-OM', '订单管理', '#', '', '1', '', 'Y', 'M', '168230', 'SNDA-wangjinhua', '2011-12-28 14:42:29.000000', '2011-12-28 14:42:29.000000', '');";
 		String menu2 = "values ('"
 				+ tSqlId
 				+ "', '"
 				+ tId
 				+ "', '"
 				+ tName
 				+ "', '"
 				+ tActionUrl
 				+ "', '', '"
 				+ tSort
 				+ "', '"
 				+ tContextPath
 				+ "', 'Y', '"
 				+ tActionType
 				+ "', '"
 				+ tParentId
 				+ "', 'SNDA-wangjinhua', '2011-12-28 14:42:29.000000', '2011-12-28 14:42:29.000000', '');\n\n";
 		sql.append(menu1);
 		sql.append(menu2);
 	}
 
 }
