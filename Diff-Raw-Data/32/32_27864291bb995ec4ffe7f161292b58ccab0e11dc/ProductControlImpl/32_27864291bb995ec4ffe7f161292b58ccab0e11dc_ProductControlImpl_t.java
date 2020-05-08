 package com.smit.dao;
 
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Expression;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 import com.smit.util.DaoException;
 import com.smit.util.SmitPage;
 import com.smit.vo.CertifiedProduct;
 import com.smit.vo.Device;
 import com.smit.vo.Order;
 import com.smit.vo.OrderAndOption;
 import com.smit.vo.TestOption;
 import com.smit.web.control.action.Page;
 
 public class ProductControlImpl extends HibernateDaoSupport implements ProductControlDao{
 
 	private SessionFactory sessionFactory;
 	
 	public void sessionFactory(SessionFactory sessionFactory){
 		this.sessionFactory = sessionFactory;
 	}
 
 	//method of testOption =====================================
 	@Override
 	public boolean insertOption(TestOption option) {
 		this.getHibernateTemplate().save(option);
 		return true;
 	}
 
 	@Override
 	public boolean updateOption(TestOption option) {
 		this.getHibernateTemplate().saveOrUpdate(option);
 		return true;
 	}
 
 	@Override
 	public boolean deleteOption(TestOption option) {
 		this.getHibernateTemplate().delete(option);
 		return true;
 	}
 
 	@Override
 	public TestOption loadOption(int id) {
 		TestOption testOption = null;
 		try{
 			
 			testOption = this.getHibernateTemplate().get(TestOption.class, id);
 		}catch (HibernateException e){
 			throw new DaoException("根据ID查找测试项失败！"+e.getMessage());
 		}
 		return testOption;
 	}
 
 	
 	@Override
 	public List<TestOption> getOptionsList() {
 		String hql = "from TestOption to";
 		List<TestOption> list = this.getHibernateTemplate().find(hql);
 		return list;
 	}
 
 	
 	@Override
 	public TestOption getOption(String name) {
 		String hql = "from TestOption t where t.name='"+name+"'";
 		List<TestOption> list = this.getHibernateTemplate().find(hql);
 		if(list.size()>0){
 			return list.get(0);
 		}
 		return null;
 	}
 
 	//method of device =================================
 	@Override
 	public boolean addDevice(Device device) {
 		this.getHibernateTemplate().save(device);
 		return true;
 	}
 
 	@Override
 	public boolean updateDevice(Device device) {
 		this.getHibernateTemplate().saveOrUpdate(device);
 		return true;
 	}
 
 	@Override
 	public boolean deleteDevice(Device device) {
 		this.getHibernateTemplate().delete(device);
 		return true;
 	}
 
 	@Override
 	public Device findById(int id) {
 		Device device = null;
 		try{
 			device = this.getHibernateTemplate().get(Device.class, id);
 		}catch (HibernateException e){
 			throw new DaoException("根据ID查找设备失败："+e.getMessage());
 		}
 		return device;
 	}
 
 	//method of order =========================================
 	@Override
 	public boolean insertOrder(Order order) {
 		this.getHibernateTemplate().merge(order);
 		//this.getHibernateTemplate().save(order);
 		return true;
 	}
 
 	@Override
 	public boolean updateOrder(Order order) {
 		this.getHibernateTemplate().saveOrUpdate(order);
 		return true;
 	}
 
 	@Override
 	public Order loadOrder(int id) {
 		//String hql = "from Order where Order.id="+id;
 		Order order = null;
 		try{
 			order = this.getHibernateTemplate().get(Order.class,id);
 		}catch (HibernateException e){
 			throw new DaoException("根据id查找订单失败："+e.getMessage());
 		}
 		
 		return order;
 	}
 
 	@Override
 	public List<Order> loadOrder(String order_code) {
 		String hql = "from Order o where o.order_code='"+order_code+"'";
 		List<Order> list = this.getHibernateTemplate().find(hql);
 		return list;
 	}
 
 	@Override
 	public List<Order> loadOrder(String order_code,String production_code) {
 		StringBuffer sb = new StringBuffer();
 		sb.append("from Order o ");
 		if(!("".equals(order_code))&& null != order_code){
 			sb.append("where o.order_code='"+order_code.trim()+"'");
 			if(!("".equals(production_code))&& null!= production_code){
 				sb.append(" and o.production_code='"+production_code.trim()+"'");
 			}
 		}else{
 			if(!("".equals(production_code))&& null!= production_code){
 				sb.append(" where o.production_code='"+production_code.trim()+"'");
 			}
 		}
 		//String hql = "from Order o where o.order_code='"+order_code+"'";
 		List<Order> list = this.getHibernateTemplate().find(sb.toString());
 		return list;
 	}
 
 	
 	@Override
 	public List<Order> loadOrder() {
 		String hql = "from Order";
 		return this.getHibernateTemplate().find(hql);
 	}
 
 	@Override
 	public List<Device> getDevice(String checkID) {
 		String hql = "from Device d where d.check_id='"+checkID+"'";
 		return this.getHibernateTemplate().find(hql);
 	}
 
 	//method of product =============================================
 	@Override
 	public boolean insertProduct(CertifiedProduct product) {
 		this.getHibernateTemplate().save(product);
 		return true;
 	}
 
 	@Override
 	public boolean updateProduct(CertifiedProduct product) {
 		this.getHibernateTemplate().saveOrUpdate(product);
 		return true;
 	}
 
 	@Override
 	public boolean deleteProduct(CertifiedProduct product) {
 		this.getHibernateTemplate().delete(product);
 		return true;
 	}
 
 	@Override
 	public List<CertifiedProduct> getProductList(Device device) {
 		
 		String hql = "from CertifiedProduct cp";
 		//Session session = this.getHibernateTemplate().getSessionFactory().openSession();
 		//Query query = session.createQuery(hql);
 		List<CertifiedProduct> list = this.getHibernateTemplate().find(hql);
 		//query.set
 		return list;
 	}
 
 	//中间表
 	@Override
 	public List<OrderAndOption> getOptionByCode(String order_code) {
 		String hql = "from OrderAndOption oo where oo.order_code='"+order_code+"'";
 		List<OrderAndOption> list = this.getHibernateTemplate().find(hql);
 		return list;
 	}
 
 	@Override
 	public boolean insertOrderOption(OrderAndOption oo) {
 		this.getHibernateTemplate().save(oo);
 		return true;
 	}
 
 	@Override
 	public List<Device> findByMchIdCode(String machineCode, String order_code) {
 		String hql = "from Device d where d.order_code='"+order_code+"'"+" and d.machineID='"+machineCode+"'";
 		return this.getHibernateTemplate().find(hql);
 	}
 
 	@Override
 	public List<Object[]> findMaxSn(String order_code) {
 		String hql = "select order_code,max(sn),max(mac) from Device d where d.order_code='"+order_code+"' group by order_code";
 		return this.getHibernateTemplate().find(hql);
 	}
 
 	@Override
 	public List<Device> findFailCode(String order_code, int auth_status) {
 		String hql = "from Device d where d.order_code='"+order_code+"' and d.auth_status="+auth_status;
 		return this.getHibernateTemplate().find(hql);
 	}
 
 	@Override
 	public List<Device> findDevice(String orderCode, String productCode,
 			String manuCode) {
 		Session session = this.getHibernateTemplate().getSessionFactory().openSession();
 		Criteria criteria = session.createCriteria(Device.class);
		if(!"".equals(orderCode)&&orderCode!= null){
			criteria.add(Restrictions.eq("order_code", orderCode.trim()));
 		}
		if(!"".equals(productCode)&&productCode!=null){
			criteria.add(Restrictions.eq("machineID", productCode.trim()));
 		}
 		criteria.add(Restrictions.eq("auth_status", 0));
 		List<Device> list = criteria.list();
 		return list;
 	}
 
 	@Override
 	public List<Device> findPageDevice(String orderCode, String productCode,
 			String manuCode, int begin, int num) {
 		Session session = this.getHibernateTemplate().getSessionFactory().openSession();
 		Criteria criteria = session.createCriteria(Device.class);
		if(!"".equals(orderCode)&&orderCode!= null){
 			criteria.add(Restrictions.eq("order_code", orderCode));
 		}
		if(!"".equals(productCode)&&productCode!=null){
			criteria.add(Restrictions.eq("machineID", productCode));
 		}
 		criteria.add(Restrictions.eq("auth_status", 0));
 		criteria.setFirstResult(begin);
 		criteria.setMaxResults(num);
 		List<Device> list = criteria.list();
 		return list;
 	}
 
 		
 }
