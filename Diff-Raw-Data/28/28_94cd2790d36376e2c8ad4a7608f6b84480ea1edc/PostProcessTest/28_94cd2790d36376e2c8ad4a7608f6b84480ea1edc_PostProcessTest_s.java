 package com.quanleimu.activity.test;
 
 import org.athrun.android.framework.AthrunDevice;
 import org.athrun.android.framework.AthrunTestCase;
 import org.athrun.android.framework.Test;
 import org.athrun.android.framework.viewelement.ViewElement;
 
 //all post test data
 public class PostProcessTest extends BaixingTestCase {
 
 	private static String postPosOneShouji[][] = {
 		//{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
 		{"TITLE", "描述", "二手手机测试positive1"},
 		{"TEXT", "价格", "999"},
 		{"TEXT", "联系电话", TEST_DATA_MOBILE},//取默认
 		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"}
 	};
 	
 	private static String postPosTwoShouji[][] = {
 		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
 		{"TITLE", "描述", "二手手机测试positive2"},
 		{"TEXT", "价格", "0"},
 		{"TEXT", "联系方式", TEST_DATA_MOBILE},//取默认
 		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"},
 	
 	};
 	
 	private static String postPosThreeShouji[][] = {
 		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
 		{"TITLE", "描述", "二手手机测试positive3"},
 		{"TITLE", "价格", "1000"},
 		{"TEXT", "联系方式", TEST_DATA_MOBILE},//取默认
 		{"TEXT", "具体地点", "上海市徐汇区广元西路55号"},//取默认
 	
 	};
 	
 	private static String postPosFourShouji[][] = {
 		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
 		{"TITLE", "描述", "二手手机测试positive3"},
 		{"TITLE", "价格", "1000"},
 		{"TEXT", "联系方式", "123456789"},//qq
 		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"},
 	
 	};
 	private static String postNagOneShouji[][] = {
 		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
 		{"TITLE", "描述", "二手手机测试nagetive1"},
 		{"TITLE", "价格", ""}, //留空
 		{"TEXT", "联系方式", TEST_DATA_MOBILE},
 		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"},
 	
 	};
 
 	private static String postNagTwoShouji[][] = {
 		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
 		{"TITLE", "描述", ""}, //留空
 		{"TITLE", "价格", "999"}, 
 		{"TEXT", "联系方式", TEST_DATA_MOBILE},
 		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"},
 	
 	};
 	
 	private static String postPosOneQuanzhi[][] = {
 		{"CATEGORY", "0", "全职求职简历"}, //求职简历->全职求职简历
 		{"TITLE", "工作经历", "曾经做过：电焊，服务员，宣传员，售货员，保洁员，幼教"}, //留空
 		{"TITLE", "联系方式", TEST_DATA_MOBILE}, 
 		{"TEXT", "现居住地", "上海浦东金桥博兴路"},
 		{"SELECT", "求职意向", }, //全选
 		{"TEXT", "姓名", "李四四" }, //全选
 	
 	};
 	
 	
 	public PostProcessTest() throws Exception {
 	}
 	
 	@Test
 	public void testErshouProcess() throws Exception {
 		
 		//android3.0
 		openTabbar(TAB_ID_POST);
 		//openGridByText("物品交易");
 		openPostFirstCategory(0);
 		//选择类目“二手手机”
 		openSecondCategoryByName("二手手机");
 		//清空所有已填信息
 		
 		//填入对应test data
 		String title = postEnterData(postPosOneShouji);
 		//选中某输入栏，拖动页面
 		//检查：键盘弹起时发布页面可上下拖动
 		//全部填完后检查已填内容，确保正确，未被清空
 		//点击发布按钮
 		postSend();
 		//检查发布后未停留在发布页面
 		assertTrue(afterPostSend());
 		//进入已发布列表		
 		openMyGridByText(MY_LISTING_MYAD_TEXT);
 		//取title
 		ViewElement b1= findElementByText(title);
 		//检查是否取到，取到则删除并返回
 		if (b1 != null) {
 			deleteAdByText(title);
 			return;
 		}
 		goBack();//有无都可以
 		//进入未审核通过页面
 		openMyGridByText(MY_LISTING_MYAD_APPROVE_TEXT);
 		//取发布title
 		ViewElement b2= findElementByText(title);
 		//检查是否取到，取到则删除
 		if (b2 != null) {
 			deleteAdByText(title);
 		}
 		//否则未发布成功
 		assertTrue(b1 != null || b2 != null);
 	}
 
    @Test
    public void testQuanzhiProcess() throws Exception {
 	
 	    //android3.0
 	   openTabbar(TAB_ID_POST);
 	    //选择类目“全职求职简历”
 	   openPostFirstCategory(5);
 	   openSecondCategoryByIndex(0);
 	  
 	    //清空所有已填信息
 	    //填入对应test data
 	   String title= postEnterData(postPosTwoShouji);
 	    //全部填完后检查已填内容，确保正确，未被清空
 	    //点击发布按钮
 	   postSend();
 	    //检查发布后未停留在发布页面
 	   assertTrue(afterPostSend());
 	    //进入已发布页面
 	   openMyGridByText(MY_LISTING_MYAD_TEXT);
 	   //取title
 	   ViewElement b1= findElementByText(title);
 	   //检查是否取到title，取到则删除并返回
 	   if (b1 != null){
 		   deleteAdByText(title);
 		   return;
 	   }
 	
 	   //进入未审核通过页面
 	   openMyGridByText(MY_LISTING_MYAD_APPROVE_TEXT);
 	   //取title
 	   ViewElement b2= findElementByText(title);
 	   //检查是否取到，取到则删除
 	   if (b2 != null){
 		  deleteAdByText(title);
 	   }
 	   //若都未取到，则给提示
 	   assertTrue(b1 != null || b2 != null);
    }
