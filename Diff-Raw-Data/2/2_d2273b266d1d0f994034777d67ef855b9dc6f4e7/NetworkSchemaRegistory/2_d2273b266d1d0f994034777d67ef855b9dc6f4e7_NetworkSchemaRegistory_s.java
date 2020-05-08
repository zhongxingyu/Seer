 package org.jcwal.so.network.service.impl;
 
 import javax.annotation.PostConstruct;
 
 import org.macula.base.data.vo.CriteriaType;
 import org.macula.base.data.vo.DataType;
 import org.macula.base.data.vo.FieldControl;
 import org.macula.plugins.mda.finder.domain.FinderAction;
 import org.macula.plugins.mda.finder.domain.FinderColumn;
 import org.macula.plugins.mda.finder.domain.FinderDataSet;
 import org.macula.plugins.mda.finder.domain.FinderDetailView;
 import org.macula.plugins.mda.finder.domain.FinderParam;
 import org.macula.plugins.mda.finder.domain.FinderSchema;
 import org.macula.plugins.mda.finder.domain.FinderTabView;
 import org.macula.plugins.mda.finder.service.FinderSchemaService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class NetworkSchemaRegistory {
 
 	public static final String NETWORKRESOURCE_SCHEMA = "NETWORKRESOURCE_SCHEMA";
 	public static final String SEGMENTRESOURCE_SCHEMA = "SEGMENTRESOURCE_SCHEMA";
 	public static final String DOCTOR_SCHEMA = "DOCTOR_SCHEMA";
 
 	@Autowired
 	private FinderSchemaService memorySchemaService;
 
 	@PostConstruct
 	public void registry() {
 		memorySchemaService.add(createDoctorSchema());
 		memorySchemaService.add(createNetworkResourceSchema());
 		memorySchemaService.add(createSegmentResourceSchema());
 	}
 
 	private FinderSchema createSegmentResourceSchema() {
 		FinderSchema schema = new FinderSchema();
 		schema.setCode(SEGMENTRESOURCE_SCHEMA);
 		schema.setTitle("网段资源管理");
 		schema.setRelativePath("admin/network/segmentresource/list");
 
 		FinderDataSet finderDataSet = new FinderDataSet("SEGMENT_RESOURCE_SET");
 		schema.setFinderDataSet(finderDataSet);
 
 		FinderParam param = null;
 		FinderColumn column = new FinderColumn();
 
 		column.setLabel("编号");
 		column.setColumn("id");
 		column.setName("id");
 		column.setType(DataType.Long);
 		column.setPkey(true);
 		column.setVisible(false);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("类型");
 		column.setColumn("TYPE");
 		column.setName("TYPE");
 		column.setType(DataType.String);
 		column.setVisible(false);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		//		param = new FinderParam(column, "segment_type_list");
 		//		param.setEnableSearch(true);
 		//		param.setEnableFilter(true);
 		//		param.setDefaultCriteriaType(CriteriaType.Equals);
 		//		param.setFieldControl(FieldControl.Combox);
 		//		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("区域");
 		column.setColumn("tarea");
 		column.setName("tarea");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("IP地址段");
 		column.setColumn("tsegment");
 		column.setName("tsegment");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.StartWith);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("用途");
 		column.setColumn("tusage");
 		column.setName("tusage");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("获取方式");
 		column.setColumn("taccess");
 		column.setName("taccess");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("VLAN号");
 		column.setColumn("tvlanCode");
 		column.setName("tvlanCode");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("VLAN名");
 		column.setColumn("tvlanName");
 		column.setName("tvlanName");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("起始IP");
 		column.setColumn("tstartIp");
 		column.setName("tstartIp");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("终止IP");
 		column.setColumn("tendIp");
 		column.setName("tendIp");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("IP数");
 		column.setColumn("tipSize");
 		column.setName("tipSize");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(50);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("子网掩码");
 		column.setColumn("tmask");
 		column.setName("tmask");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("网关");
 		column.setColumn("tgateway");
 		column.setName("tgateway");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("网关设备");
 		column.setColumn("tgatewayType");
 		column.setName("tgatewayType");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("保留IP");
 		column.setColumn("tkeepIps");
 		column.setName("tkeepIps");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("保留IP数");
 		column.setColumn("tkeepIpSize");
 		column.setName("tkeepIpSize");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(50);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("启用日期");
 		column.setColumn("tvalidateDate");
 		column.setName("tvalidateDate");
 		column.setType(DataType.Date);
 		column.setFormat("yyyy-MM-dd");
 		column.setVisible(true);
 		column.setWidth(80);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("取消日期");
 		column.setColumn("tinvalidateDate");
 		column.setName("tinvalidateDate");
 		column.setType(DataType.Date);
 		column.setFormat("yyyy-MM-dd");
 		column.setVisible(true);
 		column.setWidth(80);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("线路名称");
 		column.setColumn("tline");
 		column.setName("tline");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("电路编号");
 		column.setColumn("telec");
 		column.setName("telec");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("端口");
 		column.setColumn("tport");
 		column.setName("tport");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("主DNS");
 		column.setColumn("tmainDns");
 		column.setName("tmainDns");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("备DNS");
 		column.setColumn("tsecondDns");
 		column.setName("tsecondDns");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("备注");
 		column.setColumn("comments");
 		column.setName("comments");
 		column.setType(DataType.String);
 		column.setVisible(false);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		schema.setEnableFilter(true);
 		schema.setEnableSearch(true);
 
 		FinderAction action = new FinderAction();
 		action.setLabel("新增IP段");
 		action.setHref("admin/network/segmentresource/create");
 		action.setTarget("dialog::{title: '新增IP段', width:'720',height:'680'}");
 		schema.addAction(action);
 
 		action = new FinderAction();
 		action.setLabel("编辑IP段");
 		action.setHref("admin/network/segmentresource/edit");
 		action.setTarget("dialog::{title: '编辑IP资源', width:'720',height:'680'}");
 		action.setMaxRowSelected(1);
 		action.setMinRowSelected(1);
 		schema.addAction(action);
 
 		action = new FinderAction();
 		action.setLabel("创建此段IP");
 		action.setHref("admin/network/segmentresource/segment");
 		action.setTarget("dialog::{title: '创建此段IP', width:'720',height:'280'}");
 		action.setMinRowSelected(1);
 		action.setMaxRowSelected(1);
 		schema.addAction(action);
 
 		//		action = new FinderAction();
 		//		action.setLabel("导出IP段");
 		//		action.setHref("admin/network/segmentresource/export");
 		//		action.setTarget("blank");
 		//		action.setMinRowSelected(1);
 		//		action.setConfirm("您确定要导出EXCEL吗？");
 		//		schema.addAction(action);
 
 		action = new FinderAction();
 		action.setLabel("删除IP段");
 		action.setHref("admin/network/segmentresource/delete");
 		action.setConfirm("您确定删除该IP段吗？");
 		action.setTarget("command");
 		action.setMinRowSelected(1);
 		schema.addAction(action);
 
 		FinderTabView tabView = new FinderTabView();
 		tabView.setCode("insidesegment");
 		tabView.setLabel("内网网段");
 		tabView.setFilter(" type = 'A' ");
 		tabView.setOrder(0);
 		tabView.addColumns(
 				schema,
 				"id,TYPE,tarea,tsegment,tusage,taccess,tvlanCode,tvlanName,tstartIp,tendIp,tipSize,tmask,tgateway,tgatewayType,tkeepIps,tkeepIpSize,tvalidateDate,tinvalidateDate,comments");
 		schema.addTabView(tabView);
 
 		tabView = new FinderTabView();
 		tabView.setCode("outsidesegment");
 		tabView.setLabel("互联网网段");
 		tabView.setFilter(" type = 'B' ");
 		tabView.setOrder(1);
 		tabView.addColumns(schema,
 				"id,TYPE,tline,telec,tport,tstartIp,tendIp,tipSize,tmask,tgateway,tmainDns,tsecondDns");
 		schema.addTabView(tabView);
 
 		schema.setHiddenDetailColumn(true);
 		return schema;
 	}
 
 	private FinderSchema createDoctorSchema() {
 		FinderSchema schema = new FinderSchema();
 		schema.setCode(DOCTOR_SCHEMA);
 		schema.setTitle("人员管理");
 		schema.setRelativePath("admin/network/doctor/list");
 
 		FinderDataSet finderDataSet = new FinderDataSet("NETWORK_USER_SET");
 		schema.setFinderDataSet(finderDataSet);
 
 		FinderParam param = null;
 		FinderColumn column = new FinderColumn();
 
 		column.setLabel("编号");
 		column.setColumn("id");
 		column.setName("id");
 		column.setType(DataType.Long);
 		column.setPkey(true);
 		column.setVisible(false);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("帐号");
 		column.setColumn("USERNAME");
 		column.setName("USERNAME");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("姓名");
 		column.setColumn("NICKNAME");
 		column.setName("NICKNAME");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("岗位");
 		column.setColumn("TITLE");
 		column.setName("TITLE");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("电话");
 		column.setColumn("TELEPHONE");
 		column.setName("TELEPHONE");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("手机");
 		column.setColumn("MOBILE");
 		column.setName("MOBILE");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		schema.setEnableFilter(true);
 		schema.setEnableSearch(true);
 
 		FinderAction action = new FinderAction();
 		action.setLabel("新增");
 		action.setHref("admin/network/doctor/create");
 		action.setTarget("dialog::{title: '新增', width:'720',height:'280'}");
 		schema.addAction(action);
 
 		action = new FinderAction();
 		action.setLabel("编辑");
 		action.setHref("admin/network/doctor/edit");
 		action.setTarget("dialog::{title: '编辑', width:'720',height:'250'}");
 		action.setMaxRowSelected(1);
 		action.setMinRowSelected(1);
 		schema.addAction(action);
 
 		action = new FinderAction();
 		action.setLabel("删除");
 		action.setHref("admin/network/doctor/delete");
 		action.setConfirm("您确定删除吗？");
 		action.setTarget("command");
 		action.setMinRowSelected(1);
 		schema.addAction(action);
 
 		action = new FinderAction();
 		action.setLabel("修改密码");
 		action.setHref("admin/network/doctor/changepassword");
 		action.setTarget("dialog::{title: '修改密码', width:'580',height:'280'}");
 		action.setMaxRowSelected(1);
 		action.setMinRowSelected(1);
 		schema.addAction(action);
 
 		action = new FinderAction();
 		action.setLabel("角色授权");
 		action.setMinRowSelected(1);
 		action.setMaxRowSelected(1);
 		action.setTarget("dialog::{title:'角色授权',width:'800',height:'600',type:'POST'}");
 		action.setHref("admin/macula-base/sysuser/admingrant");
 		schema.addAction(action);
 
 		schema.setHiddenDetailColumn(true);
 		return schema;
 	}
 
 	private FinderSchema createNetworkResourceSchema() {
 		FinderSchema schema = new FinderSchema();
 		schema.setCode(NETWORKRESOURCE_SCHEMA);
 		schema.setTitle("IP资源管理");
 		schema.setRelativePath("admin/network/networkresource/list");
 
 		FinderDataSet finderDataSet = new FinderDataSet("NETWORK_RESOURCE_SET");
 		schema.setFinderDataSet(finderDataSet);
 
 		FinderParam param = null;
 		FinderColumn column = new FinderColumn();
 
 		column.setLabel("编号");
 		column.setColumn("id");
 		column.setName("id");
 		column.setType(DataType.Long);
 		column.setPkey(true);
 		column.setVisible(false);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("IP地址");
 		column.setColumn("IP");
 		column.setName("IP");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.StartWith);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("类型");
 		column.setColumn("TYPE");
 		column.setName("TYPE");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, "network_type_list");
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Equals);
 		param.setFieldControl(FieldControl.Combox);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("可用");
 		column.setColumn("tusable");
 		column.setName("tusable");
 		column.setType(DataType.Boolean);
 		column.setVisible(true);
 		column.setWidth(50);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, "network_usable_list");
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Equals);
 		param.setFieldControl(FieldControl.Combox);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("MAC地址");
 		column.setColumn("tmac");
 		column.setName("tmac");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		column = new FinderColumn();
 		column.setLabel("设备类型");
 		column.setColumn("tgroup");
 		column.setName("tgroup");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, "network_group_list");
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Equals);
 		param.setFieldControl(FieldControl.Combox);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("用途");
 		column.setColumn("tusage");
 		column.setName("tusage");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("设备名称");
 		column.setColumn("tname");
 		column.setName("tname");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("应用系统");
 		column.setColumn("tapplication");
 		column.setName("tapplication");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("操作系统");
 		column.setColumn("toperation");
 		column.setName("toperation");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("网络区域");
 		column.setColumn("tarea");
 		column.setName("tarea");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("所在机房");
 		column.setColumn("troom");
 		column.setName("troom");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("机架位置");
 		column.setColumn("tposition");
 		column.setName("tposition");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("交换机IP");
 		column.setColumn("tswitchIp");
 		column.setName("tswitchIp");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("交换机端口");
 		column.setColumn("tswitchPort");
 		column.setName("tswitchPort");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("配线架号");
 		column.setColumn("tdist");
 		column.setName("tdist");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		//////////////////////////////////外网///////////////////////////////////
 
 		column = new FinderColumn();
 		column.setLabel("域名");
 		column.setColumn("tdomain");
 		column.setName("tdomain");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("VS IP");
 		column.setColumn("tvsIp");
 		column.setName("tvsIp");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("F5 IP");
 		column.setColumn("tf5Ip");
 		column.setName("tf5Ip");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("VS IP");
 		column.setColumn("tvsIp");
 		column.setName("tvsIp");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("内网发布端口");
 		column.setColumn("tinsidePort");
 		column.setName("tinsidePort");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("公网IP");
 		column.setColumn("toutsideIp");
 		column.setName("toutsideIp");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("公网发布端口");
 		column.setColumn("toutsidePort");
 		column.setName("toutsidePort");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("服务器IP");
 		column.setColumn("tserverIp");
 		column.setName("tserverIp");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("线路名称");
 		column.setColumn("tline");
 		column.setName("tline");
 		column.setType(DataType.String);
 		column.setVisible(true);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		column = new FinderColumn();
 		column.setLabel("备注");
 		column.setColumn("comments");
 		column.setName("comments");
 		column.setType(DataType.String);
 		column.setVisible(false);
 		column.setWidth(100);
 		schema.addColumn(column);
 
 		param = new FinderParam(column, null);
 		param.setEnableSearch(true);
 		param.setEnableFilter(true);
 		param.setDefaultCriteriaType(CriteriaType.Contains);
 		param.setFieldControl(FieldControl.Text);
 		schema.addParam(param);
 
 		schema.setEnableFilter(true);
 		schema.setEnableSearch(true);
 
 		FinderAction action = new FinderAction();
 		action.setLabel("新增IP资源");
 		action.setHref("admin/network/networkresource/create");
 		action.setTarget("dialog::{title: '新增IP资源', width:'720',height:'680'}");
 		schema.addAction(action);
 
 		action = new FinderAction();
 		action.setLabel("编辑IP资源");
 		action.setHref("admin/network/networkresource/edit");
 		action.setTarget("dialog::{title: '编辑IP资源', width:'720',height:'680'}");
 		action.setMaxRowSelected(1);
 		action.setMinRowSelected(1);
 		schema.addAction(action);
 
 		action = new FinderAction();
 		action.setLabel("导出IP资源");
 		action.setHref("admin/network/networkresource/export");
 		action.setTarget("blank");
 		action.setMinRowSelected(1);
 		action.setConfirm("您确定要导出EXCEL吗？");
 		schema.addAction(action);
 
 		action = new FinderAction();
 		action.setLabel("删除IP资源");
 		action.setHref("admin/network/networkresource/delete");
 		action.setConfirm("您确定删除该IP资源吗？");
 		action.setTarget("command");
 		action.setMinRowSelected(1);
 		schema.addAction(action);
 
 		FinderTabView tabView = new FinderTabView();
 		tabView.setCode("insideips");
 		tabView.setLabel("内网办公区");
 		tabView.setFilter(" type = 'A' ");
 		tabView.setOrder(0);
 		tabView.addColumns(
 				schema,
 				"id,IP,TYPE,tusable,tmac,tgroup,tmodel,tname,tapplication,tusage,toperation,tarea,troom,tposition,tswitchIp,tswitchPort,tdist");
 		schema.addTabView(tabView);
 
 		tabView = new FinderTabView();
 		tabView.setCode("insideserver");
 		tabView.setLabel("内网服务器区");
 		tabView.setFilter(" type = 'B' ");
 		tabView.setOrder(1);
 		tabView.addColumns(
 				schema,
 				"id,IP,TYPE,tusable,tmac,tgroup,tmodel,tname,tapplication,tusage,toperation,tarea,troom,tposition,tswitchIp,tswitchPort,tdist");
 
 		schema.addTabView(tabView);
 
 		tabView = new FinderTabView();
 		tabView.setCode("outsideserver");
 		tabView.setLabel("F5发布区");
 		tabView.setFilter(" type = 'C' ");
 		tabView.setOrder(2);
 		tabView.addColumns(schema,
 				"id,IP,TYPE,tusable,tdomain,tvsIp,tf5Ip,tinsidePort,toutsideIp,toutsidePort,tserverIp,tusage,tapplication,troom");
 		schema.addTabView(tabView);
 
 		tabView = new FinderTabView();
 		tabView.setCode("networkserver");
 		tabView.setLabel("互联网发布区");
 		tabView.setFilter(" type = 'D' ");
 		tabView.setOrder(3);
 		tabView.addColumns(schema,
				"id,IP,TYPE,tusable,tdomain,tf5Ip,toutsideIp,tserverIp,tusage,tapplication,tline,troom");
 		schema.addTabView(tabView);
 
 		tabView = new FinderTabView();
 		tabView.setCode("allips");
 		tabView.setLabel("所有IP资源");
 		tabView.setFilter(" 1=1 ");
 		tabView.setOrder(4);
 		schema.addTabView(tabView);
 
 		FinderDetailView detailView = new FinderDetailView();
 		detailView.setCode("detail");
 		detailView.setLabel("详细信息");
 		detailView.setHref("admin/network/networkresource/detail");
 		schema.addDetailView(detailView);
 
 		detailView = new FinderDetailView();
 		detailView.setCode("segdetail");
 		detailView.setLabel("网段信息");
 		detailView.setHref("admin/network/networkresource/segdetail");
 		schema.addDetailView(detailView);
 
 		return schema;
 	}
 }
