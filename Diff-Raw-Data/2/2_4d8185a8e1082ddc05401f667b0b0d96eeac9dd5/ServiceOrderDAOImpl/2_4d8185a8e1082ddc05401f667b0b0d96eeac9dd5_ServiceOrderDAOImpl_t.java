 package com.twobytes.repair.dao;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.transform.Transformers;
 import org.hibernate.type.IntegerType;
 import org.hibernate.type.StringType;
 import org.hibernate.type.TimestampType;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.twobytes.model.ServiceOrder;
 import com.twobytes.report.form.NumRepairByEmpReportForm;
 import com.twobytes.report.form.NumRepairReportForm;
 
 @Repository
 public class ServiceOrderDAOImpl implements ServiceOrderDAO {
 
 	@Autowired
 	private SessionFactory sessionFactory;
 
 	private SimpleDateFormat sdfDateSQL = new SimpleDateFormat(
 			"yyyy-MM-dd", new Locale("US"));
 	private SimpleDateFormat sdfDate = new SimpleDateFormat(
 			"dd/MM/yyyy", new Locale("US"));
 	
 	@Override
 	public boolean save(ServiceOrder serviceOrder) throws Exception{
 		Session session = sessionFactory.getCurrentSession();
 //		try{
 			session.saveOrUpdate(serviceOrder);
 //		}catch(Exception e){
 //			e.printStackTrace();
 //			return false;
 //		}
 		//throw new Exception();
 		return true;
 	}
 
 	@Override
 	public ServiceOrder selectByID(String serviceOrderID) throws Exception{
 		ServiceOrder so = new ServiceOrder();
 		so = (ServiceOrder)sessionFactory.getCurrentSession().get(ServiceOrder.class, serviceOrderID);
 		return so;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<ServiceOrder> selectByCriteria(String name,
 			String startDate, String endDate, String type, String serialNo,
 			Integer rows, Integer page, String orderBy, String orderType) throws Exception{
 		StringBuilder sql = new StringBuilder();
 		sql.append("from ServiceOrder as serviceOrder where 1=1 ");
 		if(null != name && !name.equals("")){
 			sql.append("and serviceOrder.customer.name like :name ");
 		}
 //		if(null != surname && !surname.equals("")){
 //			sql.append("and serviceOrder.customer.surname like :surname ");
 //		}
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) between :startDate and :endDate ");
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) >= :startDate ");
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) <= :endDate ");
 		}
 		if(null != type && !type.equals("")){
 			sql.append("and serviceOrder.product.type.typeID = :type ");
 		}
 		if(null != serialNo && !serialNo.equals("")){
 			sql.append("and serviceOrder.product.serialNo like :serialNo ");
 		}
 		
 		sql.append("and serviceOrder.status != 'cancel' ");
 		
 		if(!orderBy.equals("")){
 			if(orderBy.equals("name")){
 				orderBy = "serviceOrder.customer.name";
 			}else if(orderBy.equals("surname")){
 				orderBy = "serviceOrder.customer.surname";
 			}else if(orderBy.equals("fullName")){
 				orderBy = "serviceOrder.customer.name "+orderType+", serviceOrder.customer.surname"; 
 			}
 			sql.append("order by "+orderBy+" "+orderType);
 		}else{
 			sql.append("order by serviceOrder.serviceOrderDate desc");
 		}
 		
 		Query q = sessionFactory.getCurrentSession().createQuery(sql.toString());
 		if(null != name && !name.equals("")) {
 			q.setString("name", name);
 		}
 //		if(null != surname && !surname.equals("")) {
 //			q.setString("surname", surname);
 //		}
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("startDate", startDate);
 			q.setString("endDate", endDate);
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			q.setString("startDate", startDate);
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("endDate", endDate);
 		}
 		if(null != type && !type.equals("")) {
 			q.setString("type", type);
 		}
 		if(null != serialNo && !serialNo.equals("")) {
 			q.setString("serialNo", serialNo);
 		}
 		List<ServiceOrder> result = q.list();
 		return result;
 	}
 	
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<ServiceOrder> selectByCriteria(String name, String startDate,
 			String endDate, String type, String serialNo, String empID,
 			Integer rows, Integer page, String orderBy, String orderType)
 			throws Exception {
 		StringBuilder sql = new StringBuilder();
 		sql.append("from ServiceOrder as serviceOrder where 1=1 ");
 		if(null != name && !name.equals("")){
 			sql.append("and serviceOrder.customer.name like :name ");
 		}
 //		if(null != surname && !surname.equals("")){
 //			sql.append("and serviceOrder.customer.surname like :surname ");
 //		}
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) between :startDate and :endDate ");
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) >= :startDate ");
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) <= :endDate ");
 		}
 		if(null != type && !type.equals("")){
 			sql.append("and serviceOrder.product.type.typeID = :type ");
 		}
 		if(null != serialNo && !serialNo.equals("")){
 			sql.append("and serviceOrder.product.serialNo like :serialNo ");
 		}
 		if(null != empID && !empID.equals("")){
 			sql.append("and empFix = :empID ");
 		}
 		
 		sql.append("and serviceOrder.status != 'cancel' ");
 		
 		if(!orderBy.equals("")){
 			if(orderBy.equals("name")){
 				orderBy = "serviceOrder.customer.name";
 			}else if(orderBy.equals("surname")){
 				orderBy = "serviceOrder.customer.surname";
 			}else if(orderBy.equals("fullName")){
 				orderBy = "serviceOrder.customer.name "+orderType+", serviceOrder.customer.surname"; 
 			}
 			sql.append("order by "+orderBy+" "+orderType);
 		}else{
 			sql.append("order by serviceOrder.serviceOrderDate desc");
 		}
 		
 		Query q = sessionFactory.getCurrentSession().createQuery(sql.toString());
 		if(null != name && !name.equals("")) {
 			q.setString("name", name);
 		}
 //		if(null != surname && !surname.equals("")) {
 //			q.setString("surname", surname);
 //		}
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("startDate", startDate);
 			q.setString("endDate", endDate);
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			q.setString("startDate", startDate);
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("endDate", endDate);
 		}
 		if(null != type && !type.equals("")) {
 			q.setString("type", type);
 		}
 		if(null != serialNo && !serialNo.equals("")) {
 			q.setString("serialNo", serialNo);
 		}
 		if(null != empID && !empID.equals("")){
 			q.setInteger("empID", Integer.parseInt(empID));
 		}
 		List<ServiceOrder> result = q.list();
 		return result;
 	}
 
 	@Override
 	public boolean edit(ServiceOrder serviceOrder) throws Exception{
 		Session session = sessionFactory.getCurrentSession();
 //		try{
 			session.update(serviceOrder);
 //		}catch(Exception e){
 //			e.printStackTrace();
 //			return false;
 //		}
 		return true;
 	}
 
 	@Override
 	public boolean delete(ServiceOrder serviceOrder, Integer employeeID) throws Exception{
 //		try{
 			serviceOrder.setStatus(ServiceOrder.CANCEL);
 			serviceOrder.setUpdatedDate(new Date());
 			serviceOrder.setUpdatedBy(employeeID);
 			sessionFactory.getCurrentSession().update(serviceOrder);
 //		}catch(Exception e){
 //			e.printStackTrace();
 //			return false;
 //		}
 		return true;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<ServiceOrder> selectNewSOByCriteria(String name,
 			String date, String type, Integer rows,
 			Integer page, String orderBy, String orderType) throws Exception{
 		StringBuilder sql = new StringBuilder();
 		sql.append("from ServiceOrder as serviceOrder where 1=1 ");
 		if(null != name && !name.equals("")){
 			sql.append("and serviceOrder.customer.name like :name ");
 		}
 		if(null != date && !date.equals("")){
 			sql.append("and DATE(serviceOrderDate) = :serviceOrderDate ");
 		}
 		if(null != type && !type.equals("")){
 			sql.append("and type = :type ");
 		}
 		
 		sql.append("and serviceOrder.status = 'new' ");
 		
 		if(!orderBy.equals("")){
 			if(orderBy.equals("name")){
 				orderBy = "serviceOrder.customer.name";
 			}else if(orderBy.equals("tel")){
 				orderBy = "serviceOrder.customer.tel";
 			}else if(orderBy.equals("mobileTel")){
 				orderBy = "serviceOrder.customer.mobileTel";
 			}
 			sql.append("order by "+orderBy+" "+orderType);
 		}else{
 			sql.append("order by serviceOrder.serviceOrderDate desc");
 		}
 		
 		Query q = sessionFactory.getCurrentSession().createQuery(sql.toString());
 		if(null != name && !name.equals("")) {
 			q.setString("name", name);
 		}
 		if(null != date && !date.equals("")) {
 			q.setString("serviceOrderDate", date);
 		}
 		if(null != type && !type.equals("")) {
 			q.setString("type", type);
 		}
 		List<ServiceOrder> result = q.list();
 		return result;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<ServiceOrder> selectSOForCloseByCriteria(String name,
 			String startDate, String endDate, String type, String serialNo,
 			Integer rows, Integer page, String orderBy, String orderType)
 			throws Exception {
 		StringBuilder sql = new StringBuilder();
 		sql.append("from ServiceOrder as serviceOrder where 1=1 ");
 		if(null != name && !name.equals("")){
 			sql.append("and serviceOrder.customer.name like :name ");
 		}else
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) between :startDate and :endDate ");
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) >= :startDate ");
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) <= :endDate ");
 		}
 		if(null != type && !type.equals("")){
 			sql.append("and serviceOrder.product.type.typeID = :type ");
 		}
 		if(null != serialNo && !serialNo.equals("")){
 			sql.append("and serviceOrder.product.serialNo like :serialNo ");
 		}
 		
 		sql.append("and serviceOrder.status in ('fixing','received') ");
 		
 		if(!orderBy.equals("")){
 			if(orderBy.equals("name")){
 				orderBy = "serviceOrder.customer.name";
 			}else if(orderBy.equals("surname")){
 				orderBy = "serviceOrder.customer.surname";
 			}else if(orderBy.equals("fullName")){
 				orderBy = "serviceOrder.customer.name "+orderType+", serviceOrder.customer.surname"; 
 			}
 			sql.append("order by "+orderBy+" "+orderType);
 		}else{
 			sql.append("order by serviceOrder.serviceOrderDate desc");
 		}
 		
 		Query q = sessionFactory.getCurrentSession().createQuery(sql.toString());
 		if(null != name && !name.equals("")) {
 			q.setString("name", name);
 		}
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("startDate", startDate);
 			q.setString("endDate", endDate);
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			q.setString("startDate", startDate);
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("endDate", endDate);
 		}
 		if(null != type && !type.equals("")) {
 			q.setString("type", type);
 		}
 		if(null != serialNo && !serialNo.equals("")) {
 			q.setString("serialNo", serialNo);
 		}
 		List<ServiceOrder> result = q.list();
 		return result;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<ServiceOrder> selectSOForCloseByCriteria(Integer employeeID, String name,
 			String startDate, String endDate, String type, String serialNo,
 			Integer rows, Integer page, String orderBy, String orderType)
 			throws Exception {
 		StringBuilder sql = new StringBuilder();
 		sql.append("from ServiceOrder as serviceOrder where serviceOrder.empFix = :employeeID ");
 		if(null != name && !name.equals("")){
 			sql.append("and serviceOrder.customer.name like :name ");
 		}else
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) between :startDate and :endDate ");
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) >= :startDate ");
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) <= :endDate ");
 		}
 		if(null != type && !type.equals("")){
 			sql.append("and serviceOrder.product.type.typeID = :type ");
 		}
 		if(null != serialNo && !serialNo.equals("")){
 			sql.append("and serviceOrder.product.serialNo like :serialNo ");
 		}
 		
 		sql.append("and serviceOrder.status in ('fixing','received','fixed') ");
 		
 		if(!orderBy.equals("")){
 			if(orderBy.equals("name")){
 				orderBy = "serviceOrder.customer.name";
 			}else if(orderBy.equals("surname")){
 				orderBy = "serviceOrder.customer.surname";
 			}else if(orderBy.equals("fullName")){
 				orderBy = "serviceOrder.customer.name "+orderType+", serviceOrder.customer.surname"; 
 			}
 			sql.append("order by "+orderBy+" "+orderType);
 		}else{
 			sql.append("order by serviceOrder.serviceOrderDate desc");
 		}
 		
 		Query q = sessionFactory.getCurrentSession().createQuery(sql.toString());
 		q.setInteger("employeeID", employeeID);
 		if(null != name && !name.equals("")) {
 			q.setString("name", name);
 		}
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("startDate", startDate);
 			q.setString("endDate", endDate);
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			q.setString("startDate", startDate);
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("endDate", endDate);
 		}
 		if(null != type && !type.equals("")) {
 			q.setString("type", type);
 		}
 		if(null != serialNo && !serialNo.equals("")) {
 			q.setString("serialNo", serialNo);
 		}
 		List<ServiceOrder> result = q.list();
 		return result;
 	}
 	
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<ServiceOrder> selectFixedSOByCriteria(String name, String date,
 			String type, Integer rows, Integer page, String orderBy,
 			String orderType) throws Exception {
 		StringBuilder sql = new StringBuilder();
 		sql.append("from ServiceOrder as serviceOrder where 1=1 ");
 		if(null != name && !name.equals("")){
 			sql.append("and serviceOrder.customer.name like :name ");
 		}
 		if(null != date && !date.equals("")){
 			sql.append("and DATE(serviceOrderDate) = :serviceOrderDate ");
 		}
 		if(null != type && !type.equals("")){
 			sql.append("and type = :type ");
 		}
 		
 		sql.append("and serviceOrder.status = '"+ServiceOrder.FIXED+"' ");
 		
 		if(!orderBy.equals("")){
 			if(orderBy.equals("name")){
 				orderBy = "serviceOrder.customer.name";
 			}else if(orderBy.equals("tel")){
 				orderBy = "serviceOrder.customer.tel";
 			}else if(orderBy.equals("mobileTel")){
 				orderBy = "serviceOrder.customer.mobileTel";
 			}
 			sql.append("order by "+orderBy+" "+orderType);
 		}else{
 			sql.append("order by serviceOrder.serviceOrderDate desc");
 		}
 		
 		Query q = sessionFactory.getCurrentSession().createQuery(sql.toString());
 		if(null != name && !name.equals("")) {
 			q.setString("name", name);
 		}
 		if(null != date && !date.equals("")) {
 			q.setString("serviceOrderDate", date);
 		}
 		if(null != type && !type.equals("")) {
 			q.setString("type", type);
 		}
 		List<ServiceOrder> result = q.list();
 		return result;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<ServiceOrder> getRepairReport(String startDate, String endDate) throws Exception {
 		StringBuilder sql = new StringBuilder();
 		sql.append("from ServiceOrder as serviceOrder where 1=1 ");
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) between :startDate and :endDate ");
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) >= :startDate ");
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) <= :endDate ");
 		}
 		
 		sql.append("and serviceOrder.status != 'cancel' ");
 		
 		sql.append("order by serviceOrder.serviceOrderDate ");
 		
 		Query q = sessionFactory.getCurrentSession().createQuery(sql.toString());
 		
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("startDate", startDate);
 			q.setString("endDate", endDate);
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			q.setString("startDate", startDate);
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("endDate", endDate);
 		}
 		
 		List<ServiceOrder> result = q.list();
 		return result;
 	}
 
 	@Override
 	public NumRepairReportForm getNumRepairReport(String date)
 			throws Exception {
 		
 		/*select openProd.numOpen, fixingProd.numFixing, fixedProd.numFixed,returnProd.numReturn from
 	    (select count(*) numOpen from serviceOrder where status = 'new' and DATE(serviceOrderDate) = '2011-09-26') openProd, 
 	    (select count(*) numReturn from serviceOrder where status = 'close' and DATE(serviceOrderDate) = '2011-09-26') returnProd, 
 	    (select count(*) numFixed from serviceOrder where status = 'fixed' and DATE(serviceOrderDate) = '2011-09-26') fixedProd,
 	    (select count(*) numFixing from serviceOrder where status in ('fixing', 'outsite') and DATE(serviceOrderDate) = '2011-09-26') fixingProd;*/
 		
 		StringBuilder sql = new StringBuilder();
 		sql.append("select openProd.numOpen numOpen, fixingProd.numFixing numFixing, fixedProd.numFixed numFixed, returnProd.numReturn numReturn from ");
 		sql.append("(select count(*) numOpen from serviceOrder where status = 'new' and DATE(serviceOrderDate) = :date) openProd, ");
 		sql.append("(select count(*) numReturn from serviceOrder where status = 'close' and DATE(serviceOrderDate) = :date) returnProd,");
 		sql.append("(select count(*) numFixed from serviceOrder where status = 'fixed' and DATE(serviceOrderDate) = :date) fixedProd,");
 		sql.append("(select count(*) numFixing from serviceOrder where status in ('fixing', 'outsite') and DATE(serviceOrderDate) = :date) fixingProd;");
 		Query q = sessionFactory.getCurrentSession().createSQLQuery(sql.toString()).addScalar("numOpen", new IntegerType()).addScalar("numFixing", new IntegerType()).addScalar("numFixed", new IntegerType()).addScalar("numReturn", new IntegerType()).setResultTransformer(Transformers.aliasToBean(NumRepairReportForm.class)).setString("date", date);
 		
 		NumRepairReportForm reportForm = (NumRepairReportForm)q.uniqueResult();
 		Date dateSQL = sdfDateSQL.parse(date);
 		reportForm.setDate(sdfDate.format(dateSQL));
 		return reportForm;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<NumRepairByEmpReportForm> getNumRepairByEmpReport(
 			String startDate, String endDate, Integer employeeID)
 			throws Exception {
		//		select c.num, emp.name, emp.surname, CONCAT(emp.name,' ',emp.surname) fullname, serviceOrderID, serviceOrderDate, startFix, endFix, appointmentDate, returnDate, CALTIMEDIFF(startFix, serviceOrderDate) diffStartFix_sec, CALTIMEDIFF(endFix, startFix) diffFix_sec, CALTIMEDIFF(appointmentDate, endFix) diffFinish_sec, CALTIMEDIFF(returnDate, endFix) diffReturn_sec from serviceOrder so, (select count(*) num from serviceOrder where empFix = 1) c, employee emp where so.empFix = 1 and so.empFix = emp.employeeID and so.status = 'close';
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT c.num numOfDoc, emp.name, emp.surname, CONCAT(emp.name,' ',emp.surname) fullName, serviceOrderID, " +
 				"serviceOrderDate, startFix, endFix, appointmentDate, returnDate, " +
 				"CALTIMEDIFF(startFix, serviceOrderDate) diffStartFix, CALTIMEDIFF(endFix, startFix) diffFix, CALTIMEDIFF(appointmentDate, endFix) diffFinish, CALTIMEDIFF(returnDate, endFix) diffReturn " +
 				"FROM serviceOrder so, (select count(*) num from serviceOrder where status = 'close' ");
 		
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) between :startDate and :endDate ");
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) >= :startDate ");
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) <= :endDate ");
 		}
 		if(employeeID != null){
 			sql.append("and empFix = :empID ");
 		}
 		sql.append(") c, employee emp ");
 		sql.append("WHERE so.empFix = emp.employeeID ");
 		
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) between :startDate and :endDate ");
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) >= :startDate ");
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			sql.append("and DATE(serviceOrderDate) <= :endDate ");
 		}
 		if(employeeID != null){
 			sql.append("and so.empFix = :empID ");
 		}
 		
 		sql.append("and so.status = 'close' ");
 		sql.append("order by fullName, so.serviceOrderDate ");
 		
 		Query q = sessionFactory.getCurrentSession().createSQLQuery(sql.toString())
 			.addScalar("numOfDoc", new IntegerType())
 			.addScalar("name", new StringType())
 			.addScalar("surname", new StringType())
 			.addScalar("fullName", new StringType())
 			.addScalar("serviceOrderID", new StringType())
 			.addScalar("serviceOrderDate", new TimestampType())
 			.addScalar("startFix", new TimestampType())
 			.addScalar("endFix", new TimestampType())
 			.addScalar("appointmentDate", new TimestampType())
 			.addScalar("returnDate", new TimestampType())
 			.addScalar("diffStartFix", new StringType())
 			.addScalar("diffFix", new StringType())
 			.addScalar("diffFinish", new StringType())
 			.addScalar("diffReturn", new StringType())
 			.setResultTransformer(Transformers.aliasToBean(NumRepairByEmpReportForm.class));
 		
 		if((null != startDate && !startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("startDate", startDate);
 			q.setString("endDate", endDate);
 		}else if((null != startDate && !startDate.equals("")) && (null == endDate || endDate.equals(""))){
 			q.setString("startDate", startDate);
 		}else if((null == startDate || startDate.equals("")) && (null != endDate && !endDate.equals(""))){
 			q.setString("endDate", endDate);
 		}
 		if(employeeID != null){
 			q.setInteger("empID", employeeID);
 		}
 		
 		List<NumRepairByEmpReportForm> retList = q.list();
 		return retList;
 	}
 	
 
 
 }
