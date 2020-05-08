 package com.taobao.ldbFastDump;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author ashu.cs
  * 
  */
 
 public class FailOverLdbFastDumpTest1 extends FailOverBaseCase{
 
 	public int ctrlDataTool(String conf, String group, String kv_name, String putstart) {
 		int waitcnt = 0;
 		if(!modify_config_file("local", test_bin+conf, "group_name", group))
 			fail("modify configure file's group_name to " + group + " failed");
 		if(!modify_config_file("local", test_bin+conf, "filename", kv_name))
 			fail("modify configure file's kvfile to " + kv_name + " failed");
		if(!modify_config_file("local", test_bin+conf, "putstart", putstart))
 			fail("modify configure file's putstart to " + putstart + " failed");
 		if("tairtool_put.conf".equals(conf)) {
 			execute_data_verify_tool(test_bin, "put");
 			while(check_process("local", "tairtool_put")!=2)
 			{
 				waitto(9);
 				if(++waitcnt>30)break;
 			}
 			if(waitcnt>30)fail("wait data tool finish time out!");
 			waitto(5);
 			if(!"successful".equals(control_sh(csList.get(0), FailOverBaseCase.tair_bin, "fastdump.sh", group + " flushmmt "+group)))
 				fail("flushmmt to " + group + " failed!");
 			return getVerifySuccessful("local", test_bin, "put.log");
 		}
 		else if("tairtool_get.conf".equals(conf)) {
 			execute_data_verify_tool(test_bin, "get");
 			return 0;
 		}
 		else
 			return -1;
 	}
 	
 	public void start_cluster_and_prepare_data() {
 		if(!batch_control_cs(csList, start, 0))
 			fail("start cs cluster failed!");
 		if(!batch_control_ds(dsList, start, 0))
 			fail("start ds cluster failed!");
 		log.debug("wait system initialize ...");
 		waitto(down_time);
 		log.info("Start Cluster Successful!");
 		if(!modify_config_file(csList.get(0), tair_bin+"etc/group.conf", "_min_data_server_count", "10"))
 			fail("modify configure file failure");
 		
 		// 1׼
 		int put1_old = ctrlDataTool("tairtool_put.conf", "group_1", "old1.kv", "0");
 		Assert.assertTrue("group_1 put successful rate small than 99%!",put1_old/put_count_float>0.99);
 		int put2_old = ctrlDataTool("tairtool_put.conf", "group_2", "old2.kv", "0");
 		Assert.assertTrue("group_2 put successful rate small than 99%!",put2_old/put_count_float>0.99);
 		log.info("Write data over!");
 		
 		// 2group״̬Ϊon
 		if(!"successful".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 setstatus group_1 on")))
 			fail("set group1 on failed!");
 		if(!"successful".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_2 setstatus group_2 on")))
 			fail("set group2 on failed!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_1", "group_status")))
 			fail("check group1 status did't on after set group1 status on!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_2", "group_status")))
 			fail("check group2 status did't on after set group2 status on!");
 	}
 	
 	@Test
 	public void testFailover_01_reset_every_group()
 	{   
 		log.info("start ldb fast dump test Failover case 01");
 		start_cluster_and_prepare_data();
 		
 		// 2ʼѭ
 		if(ctrlDataTool("tairtool_get.conf", "group_master", "old1.kv", "0") != 0)
 			fail("start read old data failed!");
 		
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		int suc_count = getKeyNumber("local", test_bin, "Successful");
 		int fail_count = getKeyNumber("local", test_bin, "fail");
 		Assert.assertTrue("get successful count smaller than 0!", suc_count > 0);
 		Assert.assertTrue("get fail_count count not 0!", fail_count == 0);
 		
 		int versionGroup_1 = check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		int versionGroup_2 = check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 		
 		//3consolecs, group1 off
 		if(!"successful".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 setstatus group_1 off")))
 			fail("shut off group1 failed!");
 		waitto(5);
 		
 	    //4cs rebuild version table, ͸group[1,2]ds
 		if(!"off".equals(getGroupKeyword(csList.get(0), "group_1", "group_status")))
 			fail("check group1 status did't off after set group1 status off!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_2", "group_status")))
 			fail("check group2 status changed to off after set group1 status off!");
 		if(!"group_1: group_status=off".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 getstatus group_1")))
 			fail("get group1 status not off!");
 		if(!"group_2: group_status=on".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_2 getstatus group_2")))
 			fail("get group2 status not on!");
 		
 	    //5clientds,versionһ, csȡgroup״̬(group1 off,group2 on)
 		if(check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log")==versionGroup_1)
 			fail("group_1 version didn't changed after set group_1 status off!");
 		if(check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log")!=versionGroup_2)
 			fail("group_2 version changed after set group_1 status off!");
 		versionGroup_1=check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		
 	    //6clientֻgroup2dsgroup_1group_2ϼgetCount
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count more than 100!", getKeyNumber("local", test_bin, "fail") < fail_count+100);
 		suc_count = getKeyNumber("local", test_bin, "Successful");
 		fail_count = getKeyNumber("local", test_bin, "fail");
 		
 		int group1_getcount = new Integer(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 stat")).intValue();
 		int group2_getcount = new Integer(control_sh(csList.get(1), tair_bin, "fastdump.sh", "group_2 stat")).intValue();
 		Assert.assertTrue(group1_getcount == 0);
 		Assert.assertTrue(group2_getcount > 0);
 		
 	    //7console()-->group1ds
 		if(!"successful".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 resetserver group_1")))
 			fail("reset group1 failed!");
 		
 	    //8ݵgroup1 flush. group1 dsˢdisk
 		int put1_new = ctrlDataTool("tairtool_put.conf", "group_1", "new1.kv", "100000");
 		Assert.assertTrue("group_1 put successful rate small than 99%!",put1_new/put_count_float>0.99);
 		
 	    //9consolegroup1 on cs
 		if(!"successful".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 setstatus group_1 on")))
 			fail("open group1 failed!");
 		waitto(5);
 		
 		//10ʹ߶
 		if(!modify_config_file("local", test_bin+"tairtool_get.conf", "filename", "new1.kv"))
 			fail("modify configure file's kv file name failed");
 		if(!sendSignal("local", "tairtool_get", "12"))//SIGUSR2
 			fail("send signal to change get group failed!");
 		
 	    //11ظ456789
 		//cs rebuild version table, ͸group[1,2]ds
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_1", "group_status")))
 			fail("check group1 status did't on after set group1 status on!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_2", "group_status")))
 			fail("check group2 status changed to off after set group1 status on!");
 		if(!"group_1: group_status=on".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 getstatus group_1")))
 			fail("get group1 status not on!");
 		if(!"group_2: group_status=on".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_2 getstatus group_2")))
 			fail("get group2 status not on!");
		
		//clientds,versionһ, csȡgroup״̬(group1 on,group2 on)
 		if(check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log")==versionGroup_1)
 			fail("group_1 version didn't changed after set group_1 status on!");
 		if(check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log")!=versionGroup_2)
 			fail("group_2 version changed after set group_1 status on!");
 		versionGroup_1=check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
		
		//clientgroupds
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
//		Assert.assertTrue("get fail_count count changed after read new data!", getKeyNumber("local", test_bin, "fail") == fail_count);
 		suc_count = getKeyNumber("local", test_bin, "Successful");
 		fail_count = getKeyNumber("local", test_bin, "fail");
		
		//group_1group_2ϼgetCount
 		group1_getcount = new Integer(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 stat")).intValue();
 		group2_getcount = new Integer(control_sh(csList.get(1), tair_bin, "fastdump.sh", "group_2 stat")).intValue();
 		Assert.assertTrue(group1_getcount > 0);
		Assert.assertTrue(group2_getcount > 0);
 		
 		//consolecs, group2 off
 		if(!"successful".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_2 setstatus group_2 off")))
 			fail("shut off group2 failed!");
 		waitto(5);
 		
 	    //console()-->group2ds
 		if(!"successful".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_2 resetserver group_2")))
 			fail("reset group2 failed!");
 	    //ݵgroup2 flush. group2 dsˢdisk
 		int put2_new = ctrlDataTool("tairtool_put.conf", "group_2", "new2.kv", "100000");
 		Assert.assertTrue("group_2 put successful rate small than 99%!",put2_new/put_count_float>0.99);
 	    //consolegroup2 on cs
 		if(!"successful".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_2 setstatus group_2 on")))
 			fail("open group1 failed!");
 		waitto(5);
 		
 	    //12clientõgroup[1,2]״̬on, ѯgroup[1,2]
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count changed after read new data!", getKeyNumber("local", test_bin, "fail") == fail_count);
 		group1_getcount = new Integer(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 stat")).intValue();
 		group2_getcount = new Integer(control_sh(csList.get(1), tair_bin, "fastdump.sh", "group_2 stat")).intValue();
 		Assert.assertTrue("group1 getCount larger than group2 1.2 times!", group1_getcount < group2_getcount * 1.2);
 		Assert.assertTrue("group2 getCount larger than group1 1.2 times!", group2_getcount < group1_getcount * 1.2);
 		
 		//end test
 		log.info("end ldb fast dump test Failover case 01");
 	}
 
 	@Test
 	public void testFailover_02_restart_one_ds_without_clear_data()
 	{ 
 		log.info("start ldb fast dump test Failover case 02");
 		start_cluster_and_prepare_data();
 		
 		// 3ʼѭ
 		if(ctrlDataTool("tairtool_get.conf", "group_master", "old1.kv", "0") != 0)
 			fail("start read old data failed!");
 		
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		int suc_count = getKeyNumber("local", test_bin, "Successful");
 		int fail_count = getKeyNumber("local", test_bin, "fail");
 		Assert.assertTrue("get successful count smaller than 0!", suc_count > 0);
 		Assert.assertTrue("get fail_count count not 0!", fail_count == 0);
 		
 		int versionGroup_1 = check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		int versionGroup_2 = check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 		
 		//4group1 ds1 쳣. crashserver down,̻
 		if(!control_ds(dsList.get(0), stop, 0))
 			fail("shut down " + dsList.get(0) + " failed!");
 		
 	    //5cs鷢ds1 쳣, rebuild version table,ds1״̬Ϊdown.
 		waitto(ds_down_time);
 		if(check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log")==versionGroup_1)
 			fail("group_1 version didn't changed after set group_1 status on!");
 		if(check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log")==versionGroup_2)
 			fail("group_2 version didn't changed after set group_1 status on!");
 		versionGroup_1=check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		versionGroup_2=check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 		
 	    //6version table.ds1.(ʱgroup1dsɼṩ)
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count more than 100!", getKeyNumber("local", test_bin, "fail") < fail_count+100);
 		suc_count = getKeyNumber("local", test_bin, "Successful");
 		fail_count = getKeyNumber("local", test_bin, "fail");
 		
 		//7鱻رյdsǷʱرбgroup״̬Ƿı
 		if("10.232.4.14:5161;".equals(getGroupKeyword(csList.get(0), "group_1", "tmp_down_server")))
 			fail("check ds 1 didn't add to group_1's tmp_down_server by read group.conf!");
 		if("group_1: tmp_down_server=10.232.4.14:5161;".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 gettmpdownsvr group_1")))
 			fail("check ds 1 didn't add to group_1's tmp_down_server by tairclient cmd!");
 		if("".equals(getGroupKeyword(csList.get(0), "group_2", "tmp_down_server")))
 			fail("check group_2's tmp_down_server not null!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_1", "group_status")))
 			fail("check group1 status changed after one ds on group1 down!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_2", "group_status")))
 			fail("check group2 status changed after one ds on group1 down!");
 		
 	    //8group ds1,start
 		if(!control_ds(dsList.get(0), start, 0))
 			fail("start " + dsList.get(0) + " failed!");
 		
 	    //9cs group1 ds1״̬downĳnot ready(Ҫ汾)
 	    //10ds1ϵݵ.
 	    //11console֪ͨcs,ds1״̬up
 		if(!"successful".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 resetserver group_1 10.232.4.14")))
 			fail("resetserver on group1 failed!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_1", "group_status")))
 			fail("check group1 status did't on after set group1 status on!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_2", "group_status")))
 			fail("check group2 status did't on after set group2 status on!");
 		
 	    //12cs rebuild version
 		waitto(down_time);
 		if(check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log")==versionGroup_1)
 			fail("group_1 version didn't changed after restart ds on group1!");
 		if(check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log")==versionGroup_2)
 			fail("group_2 version didn't changed after restart ds on group1!");
 		versionGroup_1=check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		versionGroup_2=check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 		
 	    //13client汾,ds1.
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count changed after restart ds on group1!", getKeyNumber("local", test_bin, "fail") == fail_count);
 		int group1_getcount = new Integer(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 stat")).intValue();
 		int group2_getcount = new Integer(control_sh(csList.get(1), tair_bin, "fastdump.sh", "group_2 stat")).intValue();
 		Assert.assertTrue("group1 getCount larger than group2 1.2 times!", group1_getcount < group2_getcount * 1.2);
 		Assert.assertTrue("group2 getCount larger than group1 1.2 times!", group2_getcount < group1_getcount * 1.2);
 
 		//end test
 		log.info("end ldb fast dump test Failover case 02");
 	}
 	
 	//case 03 ݺds
 	@Test
 	public void testFailover_03_restart_one_ds_after_clear_data()
 	{ 
 		log.info("start ldb fast dump test Failover case 03");
 		start_cluster_and_prepare_data();
 		
 		// 3ʼѭ
 		if(ctrlDataTool("tairtool_get.conf", "group_master", "old1.kv", "0") != 0)
 			fail("start read old data failed!");
 		
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		int suc_count = getKeyNumber("local", test_bin, "Successful");
 		int fail_count = getKeyNumber("local", test_bin, "fail");
 		Assert.assertTrue("get successful count smaller than 0!", suc_count > 0);
 		Assert.assertTrue("get fail_count count not 0!", fail_count == 0);
 		
 		int versionGroup_1 = check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		int versionGroup_2 = check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 		
 		//4group1 ds1 쳣. crashserver down,̻
 		if(!control_ds(dsList.get(0), stop, 0))
 			fail("shut down " + dsList.get(0) + " failed!");
 		
 	    //5cs鷢ds1 쳣, rebuild version table,ds1״̬Ϊdown.
 		waitto(ds_down_time);
 		if(check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log")==versionGroup_1)
 			fail("group_1 version didn't changed after set group_1 status on!");
 		if(check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log")==versionGroup_2)
 			fail("group_2 version didn't changed after set group_1 status on!");
 		versionGroup_1=check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		versionGroup_2=check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 		
 	    //6version table.ds1.(ʱgroup1dsɼṩ)
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count more than 100!", getKeyNumber("local", test_bin, "fail") < fail_count+100);
 		suc_count = getKeyNumber("local", test_bin, "Successful");
 		fail_count = getKeyNumber("local", test_bin, "fail");
 		
 		//7鱻رյdsǷʱرбgroup״̬Ƿı
 		if("10.232.4.14:5161;".equals(getGroupKeyword(csList.get(0), "group_1", "tmp_down_server")))
 			fail("check ds 1 didn't add to group_1's tmp_down_server!");
 		if("".equals(getGroupKeyword(csList.get(0), "group_2", "tmp_down_server")))
 			fail("check group_2's tmp_down_server not null!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_1", "group_status")))
 			fail("check group1 status changed after one ds on group1 down!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_2", "group_status")))
 			fail("check group2 status changed after one ds on group1 down!");
 		
 	    //8group ds1ݺ,start
 		if(!"successful".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 resetdb group_1 10.232.4.14:5161")))
 			fail("clear data on ds1 failed!");
 		if(!control_ds(dsList.get(0), start, 0))
 			fail("start " + dsList.get(0) + " failed!");
 		
 	    //9cs group1 ds1״̬downĳnot ready(Ҫ汾)
 	    //10ds1ϵݵ.
 	    //11console֪ͨcs,ds1״̬up
 		if(!"successful".equals(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 resetserver group_1 10.232.4.14")))
 			fail("resetserver on group1 failed!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_1", "group_status")))
 			fail("check group1 status did't on after set group1 status on!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_2", "group_status")))
 			fail("check group2 status did't on after set group2 status on!");
 		
 	    //12cs rebuild version
 		waitto(down_time);
 		if(check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log")==versionGroup_1)
 			fail("group_1 version didn't changed after restart ds on group1!");
 		if(check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log")==versionGroup_2)
 			fail("group_2 version didn't changed after restart ds on group1!");
 		versionGroup_1=check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		versionGroup_2=check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 		
 	    //13client汾,ds1.
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count changed after restart ds on group1!", getKeyNumber("local", test_bin, "fail") == fail_count);
 		int group1_getcount = new Integer(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 stat")).intValue();
 		int group2_getcount = new Integer(control_sh(csList.get(1), tair_bin, "fastdump.sh", "group_2 stat")).intValue();
 		Assert.assertTrue("group1 getCount smaller than group2 1.4 times!", group1_getcount * 1.4 < group2_getcount);
 		Assert.assertTrue("group2 getCount larger than group1 1.6 times!", group2_getcount < group1_getcount * 1.6);
 
 		//end test
 		log.info("end ldb fast dump test Failover case 03");
 	}
 	
 	//case 04 group_1ҵ
 	@Test
 	public void testFailover_04_restart_group1()
 	{   
 		log.info("start ldb fast dump test Failover case 04");
 		start_cluster_and_prepare_data();
 		
 		// 2ʼѭ
 		if(ctrlDataTool("tairtool_get.conf", "group_master", "old1.kv", "0") != 0)
 			fail("start read old data failed!");
 		
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		int suc_count = getKeyNumber("local", test_bin, "Successful");
 		int fail_count = getKeyNumber("local", test_bin, "fail");
 		Assert.assertTrue("get successful count smaller than 0!", suc_count > 0);
 		Assert.assertTrue("get fail_count count not 0!", fail_count == 0);
 		
 //		int versionGroup_1 = check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 //		int versionGroup_2 = check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 		
 		//3رcsgroup_1Ⱥ
 		if(!batch_control_ds(dsList1, stop, 0) || !control_cs(csList.get(0), stop, 0))
 			fail("shut down group1 and master cs failed!");
 		waitto(ds_down_time);
 		
 		//4鿴csǷл
 		if(check_keyword(csList.get(1), "MASTER_CONFIG changed 10.232.4.17:5168", tair_bin+"logs/config.log") != 1)
 			fail("check slave cs didn't changed to master cs after master cs down!");
 		log.info("slave cs changed to master cs after master cs down!");
 		
 		//5group״̬Ƿı
 		if(!"on".equals(getGroupKeyword(csList.get(1), "group_1", "group_status")))
 			fail("check group1 status changed after group1 shut down!");
 		if(!"on".equals(getGroupKeyword(csList.get(1), "group_2", "group_status")))
 			fail("check group2 status changed after group1 shut down!");
 		
 		//6tmp_down_serverǷ
 		if("10.232.4.14:5161;10.232.4.15:5161;10.232.4.16:5161;".equals(getGroupKeyword(csList.get(0), "group_1", "tmp_down_server")))
 			fail("check group_1's tmp_down_server not correct!");
 			
 	    //7clientֻgroup2dsʧδ̫
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count more than 1000!", getKeyNumber("local", test_bin, "fail") < fail_count+1000);
 		suc_count = getKeyNumber("local", test_bin, "Successful");
 		fail_count = getKeyNumber("local", test_bin, "fail");
 		
 		//8group_1group_2ϼgetCount
 		int group1_getcount = new Integer(control_sh(csList.get(1), tair_bin, "fastdump.sh", "group_1 stat")).intValue();
 		int group2_getcount = new Integer(control_sh(csList.get(1), tair_bin, "fastdump.sh", "group_2 stat")).intValue();
 		Assert.assertTrue(group1_getcount == 0);
 		Assert.assertTrue(group2_getcount > 0);
 		
 		//end test
 		log.info("end ldb fast dump test Failover case 04");
 	}
 	
 	//case 05 cs
 	@Test
 	public void testFailover_05_restart_master_cs() {
 		log.info("start ldb fast dump test Failover case 05");
 		start_cluster_and_prepare_data();
 		
 		// 2ʼѭ
 		if(ctrlDataTool("tairtool_get.conf", "group_master", "old1.kv", "0") != 0)
 			fail("start read old data failed!");
 		
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		int suc_count = getKeyNumber("local", test_bin, "Successful");
 		int fail_count = getKeyNumber("local", test_bin, "fail");
 		Assert.assertTrue("get successful count smaller than 0!", suc_count > 0);
 		Assert.assertTrue("get fail_count count not 0!", fail_count == 0);
 		
 		int versionGroup_1 = check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		int versionGroup_2 = check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 		
 		//3رcs
 		if(!control_cs(csList.get(0), stop, 0))
 			fail("shut down master cs failed!");
 		waitto(ds_down_time);
 		
 		//4鿴csǷл
 		if(check_keyword(csList.get(1), "MASTER_CONFIG changed 10.232.4.17:5168", tair_bin+"logs/config.log") != 1)
 			fail("check slave cs didn't changed to master cs after master cs down!");
 		log.info("slave cs changed to master cs after master cs down!");
 		
 		//5group״̬Ƿı
 		if(!"on".equals(getGroupKeyword(csList.get(1), "group_1", "group_status")))
 			fail("check group1 status changed after group1 shut down!");
 		if(!"on".equals(getGroupKeyword(csList.get(1), "group_2", "group_status")))
 			fail("check group2 status changed after group1 shut down!");
 		
 		//6ʧδ仯
 		waitto(10);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count changed!", getKeyNumber("local", test_bin, "fail") == fail_count);
 		suc_count = getKeyNumber("local", test_bin, "Successful");
 		fail_count = getKeyNumber("local", test_bin, "fail");
 		
 		//7cs
 		if(!control_cs(csList.get(0), start, 0))
 			fail("restart master cs failed!");
 		waitto(down_time);
 		
 		//8鿴csǷл
 		if(check_keyword(csList.get(0), "MASTER_CONFIG changed 10.232.4.14:5168", tair_bin+"logs/config.log") != 1)
 			fail("check master cs didn't changed to master cs after master cs restart!");
 		log.info("master cs changed to master cs after master cs restart!");
 		
 		//9group״̬Ƿı
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_1", "group_status")))
 			fail("check group1 status changed after group1 shut down!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_2", "group_status")))
 			fail("check group2 status changed after group1 shut down!");
 		
 		//10汾Ƿ
 		if(check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log")==versionGroup_1)
 			fail("group_1 version didn't changed after set group_1 status on!");
 		if(check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log")==versionGroup_2)
 			fail("group_2 version didn't changed after set group_1 status on!");
 		versionGroup_1=check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		versionGroup_2=check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 			
 	    //11ʧδ
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count changed!", getKeyNumber("local", test_bin, "fail") == fail_count);
 		suc_count = getKeyNumber("local", test_bin, "Successful");
 		fail_count = getKeyNumber("local", test_bin, "fail");
 		
 		//end test
 		log.info("end ldb fast dump test Failover case 05");
 	}
 	
 	//case 06 cs
 	@Test
 	public void testFailover_06_restart_all_cs() {
 		log.info("start ldb fast dump test Failover case 06");
 		start_cluster_and_prepare_data();
 		
 		// 2ʼѭ
 		if(ctrlDataTool("tairtool_get.conf", "group_master", "old1.kv", "0") != 0)
 			fail("start read old data failed!");
 		
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		int suc_count = getKeyNumber("local", test_bin, "Successful");
 		int fail_count = getKeyNumber("local", test_bin, "fail");
 		Assert.assertTrue("get successful count smaller than 0!", suc_count > 0);
 		Assert.assertTrue("get fail_count count not 0!", fail_count == 0);
 		
 		int versionGroup_1 = check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		int versionGroup_2 = check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 	
 		//3رcs
 		if(!batch_control_cs(csList, stop, 0))
 			fail("shut down cs group failed!");
 		waitto(ds_down_time);
 		
 		//4group״̬Ƿı
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_1", "group_status")))
 			fail("check group1 status changed after group1 shut down!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_2", "group_status")))
 			fail("check group2 status changed after group1 shut down!");
 		
 		//5ʧδ仯
 		waitto(10);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count changed!", getKeyNumber("local", test_bin, "fail") == fail_count);
 		suc_count = getKeyNumber("local", test_bin, "Successful");
 		fail_count = getKeyNumber("local", test_bin, "fail");
 		
 		//6cs
 		if(!batch_control_cs(csList, start, 0))
 			fail("restart cs group failed!");
 		waitto(down_time);
 		
 		//7鿴csǷл
 		if(check_keyword(csList.get(0), "MASTER_CONFIG changed 10.232.4.14:5168", tair_bin+"logs/config.log") != 1)
 			fail("check master cs didn't changed to master cs after master cs restart!");
 		log.info("master cs changed to master cs after master cs restart!");
 		
 		//8鿴versionǷ
 		if(check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log")==versionGroup_1)
 			fail("group_1 version didn't changed after set group_1 status on!");
 		if(check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log")==versionGroup_2)
 			fail("group_2 version didn't changed after set group_1 status on!");
 		versionGroup_1=check_keyword(csList.get(0),verchange_group_1,tair_bin+"logs/config.log");
 		versionGroup_2=check_keyword(csList.get(0),verchange_group_2,tair_bin+"logs/config.log");
 		
 		//9group״̬Ƿı
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_1", "group_status")))
 			fail("check group1 status changed after group1 shut down!");
 		if(!"on".equals(getGroupKeyword(csList.get(0), "group_2", "group_status")))
 			fail("check group2 status changed after group1 shut down!");
 			
 	    //10ʧδ
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count changed!", getKeyNumber("local", test_bin, "fail") == fail_count);
 		suc_count = getKeyNumber("local", test_bin, "Successful");
 		fail_count = getKeyNumber("local", test_bin, "fail");
 		
 		//end test
 		log.info("end ldb fast dump test Failover case 06");
 	}
 	
 	//case 07 group
 	@Test
 	public void testFailover_07_shut_off_net_between_two_group() {
 		log.info("start ldb fast dump test Failover case 07");
 		start_cluster_and_prepare_data();
 		
 		// 2ʼѭ
 		if(ctrlDataTool("tairtool_get.conf", "group_master", "old1.kv", "0") != 0)
 			fail("start read old data failed!");
 		
 		waitto(15);
 		if(!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		int suc_count = getKeyNumber("local", test_bin, "Successful");
 		int fail_count = getKeyNumber("local", test_bin, "fail");
 		Assert.assertTrue("get successful count smaller than 0!", suc_count > 0);
 		Assert.assertTrue("get fail_count count not 0!", fail_count == 0);
 		
 //		int versionGroup_1 = check_keyword(csList.get(0), verchange_group_1, tair_bin + "logs/config.log");
 //		int versionGroup_2 = check_keyword(csList.get(0), verchange_group_2, tair_bin + "logs/config.log");
 
 		// Ͽgroup
 		if (!"0".equals(control_sh(csList.get(0), tair_bin, "netctrl.sh", "shut")))
 			fail("shut off net on 10.232.4.14 failed!");
 		if (!"0".equals(control_sh(csList.get(1), tair_bin, "netctrl.sh", "shut")))
 			fail("shut off net on 10.232.4.17 failed!");
 
 		// getӰ
 		waitto(15);
 		if (!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		suc_count = getKeyNumber("local", test_bin, "Successful");
 		fail_count = getKeyNumber("local", test_bin, "fail");
 		Assert.assertTrue("get successful count smaller than 0!", suc_count > 0);
 		Assert.assertTrue("get fail_count count not 0!", fail_count == 0);
 
 		// getCountȫתƵgroup1
 		int group1_getcount = new Integer(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 stat")).intValue();
 		int group2_getcount = new Integer(control_sh(csList.get(1), tair_bin, "fastdump.sh", "group_2 stat")).intValue();
 		Assert.assertTrue("group2_getcount not 0!", group2_getcount == 0);
 		Assert.assertTrue("group1_getcount not larger than 0!", group1_getcount > 0);
 
 		// ָ
 		if (!"0".equals(control_sh(csList.get(0), tair_bin, "netctrl.sh", "recover")))
 			fail("shut off net on 10.232.4.14 failed!");
 		if (!"0".equals(control_sh(csList.get(1), tair_bin, "netctrl.sh", "recover")))
 			fail("shut off net on 10.232.4.17 failed!");
 
 		// getӰ
 		waitto(15);
 		if (!sendSignal("local", "tairtool_get", "10"))
 			fail("send signal 10 to tairtool_get failed!");
 		waitto(2);
 		Assert.assertTrue("get successful count not changed after recover net!", getKeyNumber("local", test_bin, "Successful") > suc_count);
 		Assert.assertTrue("get fail_count count changed after recover net!", getKeyNumber("local", test_bin, "fail") == fail_count);
 
 		// getCountͬ
 		group1_getcount = new Integer(control_sh(csList.get(0), tair_bin, "fastdump.sh", "group_1 stat")).intValue();
 		group2_getcount = new Integer(control_sh(csList.get(1), tair_bin, "fastdump.sh", "group_2 stat")).intValue();
 		Assert.assertTrue("group1 getCount larger than group2 1.2 times!", group1_getcount < group2_getcount * 1.2);
 		Assert.assertTrue("group2 getCount larger than group1 1.2 times!", group2_getcount < group1_getcount * 1.2);
 
 		//end test
 		log.info("end ldb fast dump test Failover case 07");
 	}
 
 	@Before
 	public void setUp()
 	{
 		log.info("enter setUp!");
 		log.info("clean tool and cluster!");
 		clean_tool("local");
 		reset_cluster(csList,dsList);
 		if(!modify_config_file(csList.get(0), tair_bin+"etc/group.conf", "group_status", "off"))
 			fail("modify configure file failure");
 		if(!modify_config_file(csList.get(0), tair_bin+"etc/group.conf", "_min_data_server_count", "3"))
 			fail("modify configure file failure");
 		if(!batch_modify(csList, tair_bin+"etc/group.conf", "tmp_down_server", " "))
 			fail("modify configure file failure");
 //		if(!batch_modify(csList, tair_bin+"etc/group.conf", "_copy_count", "1"))
 //            fail("modify configure file failure");
 //        if(!batch_modify(dsList, tair_bin+"etc/group.conf", "_copy_count", "1"))
 //            fail("modify configure file failure");
 	}
 	
 	@After
 	public void tearDown()
 	{
 		log.info("enter tearDown!");
 //		log.info("clean tool and cluster!");
 //		clean_tool("local");
 //		reset_cluster(csList,dsList);
 	}
 }
