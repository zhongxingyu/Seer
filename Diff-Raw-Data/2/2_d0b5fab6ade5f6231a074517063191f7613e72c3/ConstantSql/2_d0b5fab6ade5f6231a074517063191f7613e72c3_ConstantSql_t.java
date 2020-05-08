 package com.ejustech.iron.common;
 
 /***
  * SQL定义
  *
  */
 public class ConstantSql {
 	/***
 	 * 根据用户名，在M_User表中检索，查看该用户是否存在的SQL
 	 */
 	public static final String GET_COUNTS_BY_USER_ID = "SELECT COUNT(userID) as userCOUNTs FROM m_user where userID = ?";
 
 	/***
 	 * 数据输入用SQL
 	 */
 	public static final String T_INRO_INFO_INSERT = "INSERT INTO ironinfo (`riqi`, `qihao`, `luci`, `junpin`, `guige`, `shengchanluhao`, `fanyingqihao`, `shiyongcishu`, `mg`, `ticl`, `maozhong`, `jingzhong`, `chengpinlv`, `fe`, `si`, `cl`, `c`, `n`, `o`, `h`, `mn`, `hb`, `dengji_hanmeng`, `kaohedengji_chumeng`, `gongyitiaozheng`, `gongyishiyan`, `dipi`, `shangmao`, `pabi`, `feidipi`, `feishangmao`, `feipabi`, `feitaifen`, `cixuan`, `shouxuanfeiliao`, `sunhao`, `zongpaimeiliang`, `chuluzhenkongdu`, `huanyuanzuigaowendu`, `zhengliugaoheng`, `zhuanzhengliu`, `jiashouci`, `jiamoci`, `tongdao`, `shengchanguzhang`, `beizhushuoming`, id, `ticl_query_condition`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
 
 	/***
 	 * 用非军品数据更新军品部分项目数据，关联条件：炉次
 	 */
 	public static final String T_INRO_INFO_UPDATE_JUNPIN_NOT_INPUT = "UPDATE ironinfo AS junpinT,ironinfo AS notjunpinT SET junpinT.qihao = notjunpinT.qihao,junpinT.luci = notjunpinT.luci,junpinT.shengchanluhao = notjunpinT.shengchanluhao,junpinT.fanyingqihao = notjunpinT.fanyingqihao,junpinT.shiyongcishu = notjunpinT.shiyongcishu,junpinT.gongyishiyan = notjunpinT.gongyishiyan,junpinT.zongpaimeiliang = notjunpinT.zongpaimeiliang,junpinT.chuluzhenkongdu = notjunpinT.chuluzhenkongdu,junpinT.huanyuanzuigaowendu = notjunpinT.huanyuanzuigaowendu,junpinT.zhengliugaoheng = notjunpinT.zhengliugaoheng,junpinT.zhuanzhengliu = notjunpinT.zhuanzhengliu,junpinT.jiashouci = notjunpinT.jiashouci,junpinT.jiamoci = notjunpinT.jiamoci,junpinT.tongdao = notjunpinT.tongdao,junpinT.chengpinlv = notjunpinT.chengpinlv,junpinT.shengchanguzhang = notjunpinT.shengchanguzhang,junpinT.gongyitiaozheng = notjunpinT.gongyitiaozheng,junpinT.ticl_query_condition = notjunpinT.ticl_query_condition WHERE junpinT.luci = notjunpinT.luci and junpinT.id = ?";
 
 	/***
 	 * 信息补全用，检索未输入完全的数据，flag is null
 	 */
 	public static final String T_INRO_INFO_SELECT_ALL = "SELECT id, riqi, qihao, luci, junpin, guige, shengchanluhao, fanyingqihao, shiyongcishu, mg, ticl, maozhong, jingzhong, chengpinlv, fe, si, cl, c, n, o, h, mn, hb, dengji_hanmeng, kaohedengji_chumeng, gongyitiaozheng, gongyishiyan, dipi, shangmao, pabi, feidipi, feishangmao, feipabi, feitaifen, cixuan, shouxuanfeiliao, sunhao, zongpaimeiliang, chuluzhenkongdu, huanyuanzuigaowendu, zhengliugaoheng, zhuanzhengliu, jiashouci, jiamoci, tongdao, shengchanguzhang, beizhushuoming FROM ironinfo where luci in (SELECT luci FROM ironinfo where flag is null) order by id";
 
 	/***
 	 * 信息更新时，检索用。根据选中项目的id，检索出炉次，然后检索炉次所有的军品和非军品数据
 	 */
 	public static final String T_INRO_INFO_SELECT_SINGLE = "SELECT id, riqi, qihao, luci, junpin, guige, shengchanluhao, fanyingqihao, shiyongcishu, mg, ticl, maozhong, jingzhong, chengpinlv, fe, si, cl, c, n, o, h, mn, hb, dengji_hanmeng, kaohedengji_chumeng, gongyitiaozheng, gongyishiyan, dipi, shangmao, pabi, feidipi, feishangmao, feipabi, feitaifen, cixuan, shouxuanfeiliao, sunhao, zongpaimeiliang, chuluzhenkongdu, huanyuanzuigaowendu, zhengliugaoheng, zhuanzhengliu, jiashouci, jiamoci, tongdao, shengchanguzhang, beizhushuoming FROM ironinfo where luci = (SELECT luci FROM ironinfo where id = ?) order by id";
 
 	/***
 	 * 信息更新和信息补全用SQL，根据炉次和规格确定明细行一条记录，目前：炉次和规格不可以编辑
 	 */
 	public static final String T_INRO_INFO_UPDATE = "UPDATE ironinfo SET `riqi` = ?, `qihao` = ?, `junpin` = ?, `shengchanluhao` = ?, `fanyingqihao` = ?, `shiyongcishu` = ?, `mg` = ?, `ticl` = ?, `maozhong` = ?, `jingzhong` = ?, `chengpinlv` = ?, `fe` = ?, `si` = ?, `cl` = ?, `c` = ?, `n` = ?, `o` = ?, `h` = ?, `mn` = ?, `hb` = ?, `dengji_hanmeng` = ?, `kaohedengji_chumeng` = ?, `gongyitiaozheng` = ?, `gongyishiyan` = ?, `dipi` = ?, `shangmao` = ?, `pabi` = ?, `feidipi` = ?, `feishangmao` = ?, `feipabi` = ?, `feitaifen` = ?, `cixuan` = ?, `shouxuanfeiliao` = ?, `sunhao` = ?, `zongpaimeiliang` = ?, `chuluzhenkongdu` = ?, `huanyuanzuigaowendu` = ?, `zhengliugaoheng` = ?, `zhuanzhengliu` = ?, `jiashouci` = ?, `jiamoci` = ?, `tongdao` = ?, `shengchanguzhang` = ?, `beizhushuoming` = ? , `ticl_query_condition` = ? where `luci` = ? and `guige` = ?";
 
 	/***
 	 * 更新DB中的yue字段，根据日期字段
 	 */
 	public static final String T_INRO_INFO_UPDATE_YUE = "UPDATE ironinfo SET yue = MONTH(riqi) where id = ?";
 
 	/***
 	 * 更新数据完全输入的Flag，每次登陆或保存数据时调用
 	 */
	public static final String T_INRO_INFO_UPDATE_FLG_INSER = "UPDATE ironinfo SET flag = '1' WHERE `riqi` <> '' AND `qihao` <> '' AND `luci` <> '' AND `guige` <> '' AND `shengchanluhao` <> '' AND `fanyingqihao` <> '' AND `shiyongcishu` <> '' AND `jingzhong` <> '' AND `chengpinlv` <> '' AND `fe` <> '' AND `si` <> '' AND `cl` <> '' AND `c` <> '' AND `n` <> '' AND `o` <> '' AND `h` <> '' AND `mn` <> '' AND `hb` <> '' AND `dengji_hanmeng` <> '' AND `kaohedengji_chumeng` <> '' AND `dipi` <> '' AND `shangmao` <> '' AND `pabi` <> '' AND `feidipi` <> '' AND `feishangmao` <> '' AND `feipabi` <> '' AND `feitaifen` <> '' AND `cixuan` <> '' AND `shouxuanfeiliao` <> '' AND `sunhao` <> '' AND id = ?";
 
 	/***
 	 * 确认炉次是否存在，目前未使用
 	 */
 	public static final String T_INRO_INFO_KEY_LUCI = "SELECT COUNT(luci) as cnt FROM ironinfo where luci = ?";
 
 	/***
 	 * 检索最大ID，每次数据登录时用
 	 */
 	public static final String T_INRO_INFO_MAX_ID = "SELECT IFNULL(MAX(id), 0) + 1 as maxid FROM ironinfo";
 }
