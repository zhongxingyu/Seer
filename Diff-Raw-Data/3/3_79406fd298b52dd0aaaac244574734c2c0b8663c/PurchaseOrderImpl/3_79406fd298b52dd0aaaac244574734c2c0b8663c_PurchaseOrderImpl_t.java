 package org.np.stoman.ajax.interfaze.impl;
 
 import static org.np.stoman.dao.support.HibernateSupport.getHibernateSupport;
 import static org.np.stoman.dao.support.Order.ASC;
 import static org.np.stoman.dao.support.Restrict.EQ;
 import static org.np.stoman.dao.support.Restrict.IN;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.FetchMode;
 import org.np.stoman.ajax.interfaze.PurchaseOrder;
 import org.np.stoman.dao.support.CriteriaBuilder;
 import org.np.stoman.persistence.Addresses;
 import org.np.stoman.persistence.Materials;
 import org.np.stoman.persistence.PurchaseMaterials;
 import org.np.stoman.persistence.PurchaseOrders;
 import org.np.stoman.persistence.Ranks;
 import org.np.stoman.persistence.Users;
 import org.np.stoman.persistence.VendorMaterials;
 import org.np.stoman.persistence.Vendors;
 
 public class PurchaseOrderImpl extends BaseImpl implements PurchaseOrder {
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public PurchaseOrders generate(PurchaseOrders po,
 			List<PurchaseMaterials> pms, Map<String, VendorMaterials> pmLinker) {
 		Calendar cal = Calendar.getInstance();
 		Date today = cal.getTime();
 		Users user = new Users();
 		String vmIds = "";
 		user.setUserId(1);
 		getHibernateSupport().save(po);
 		CriteriaBuilder cb = new CriteriaBuilder(PurchaseMaterials.class);
 		// I know this is stupid, but after 4 hrs of this all-delete-orphan
 		// mishap, I am ok.
 		List<PurchaseMaterials> pmsP = getHibernateSupport().get(
 				cb.getCriteria(),
 				EQ.restrict(new Object[] {
 						cb.wrap("purchaseOrders.purchaseOrderId"),
 						po.getPurchaseOrderId() }));
 		for (PurchaseMaterials pm : pmsP)
 			getHibernateSupport().delete(pm);
 		for (PurchaseMaterials pm : pms) {
 			VendorMaterials vmR = pmLinker.get(pm.getData());
 			if (vmR.getVendorMaterialId() < 0) {
 				vmR.setVendorMaterialId(null);
 				cb = new CriteriaBuilder(Vendors.class);
 				List<Vendors> vendors = getHibernateSupport().get(
 						cb.getCriteria(),
 						EQ.restrict(new Object[] { "name",
 								vmR.getVendors().getName() }));
 				if (vendors.isEmpty()) {
 					Vendors v = vmR.getVendors();
					Addresses a = new Addresses();// getHibernateSupport().get(Addresses.class,
													// 1);
 					v.setAddresses(a);
 					getHibernateSupport().save(v);
 				} else {
 					vmR.setVendors(vendors.get(0));
 				}
 				cb = new CriteriaBuilder(Materials.class);
 				List<Materials> materials = getHibernateSupport().get(
 						cb.getCriteria(),
 						EQ.restrict(new Object[] { "name",
 								vmR.getMaterials().getName() }));
 				if (materials.isEmpty()) {
 					Materials m = vmR.getMaterials();
 					getHibernateSupport().save(m);
 				} else {
 					vmR.setMaterials(materials.get(0));
 				}
 				vmR.setPricePerQty(pm.getOrderedPricePerQty() != null ? pm
 						.getOrderedPricePerQty().floatValue() : 0);
 				vmR.setUsers(user);
 				vmR.setPriceStartDate(today);
 				vmR.setModifiedDate(today);
 				getHibernateSupport().save(vmR);
 				vmIds += pm.getData() + "=" + vmR.getVendorMaterialId() + ":";
 
 			} else {
 				vmR = getHibernateSupport().get(VendorMaterials.class,
 						pmLinker.get(pm.getData()).getVendorMaterialId());
 				/*
 				 * if (!vmR.getPricePerQty().equals(
 				 * pmLinker.get(pm.getData()).getPricePerQty())) {
 				 * vmR.setPricePerQty(pmLinker.get(pm.getData())
 				 * .getPricePerQty()); vmR.setModifiedDate(today);
 				 * vmR.setUsers(user); // TODO: move to log table before //
 				 * updating the price getHibernateSupport().save(vmR); }
 				 */
 			}
 			pm.setVendorMaterials(vmR);
 			pm.setPurchaseOrders(po);
 			pm.setDate(today);
 			pm.setUsers(user);
 			getHibernateSupport().save(pm);
 		}
 
 		po.setData(vmIds);
 		return po;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public VendorMaterials getVendorMaterial(String vendor, String material) {
 		CriteriaBuilder cb = new CriteriaBuilder(VendorMaterials.class);
 		cb.getCriteria().setFetchMode("vendors", FetchMode.JOIN);
 		List<VendorMaterials> vms;
 		if (vendor != null)
 			vms = getHibernateSupport().get(
 					cb.getCriteria(),
 					EQ.restrict(new Object[] { cb.wrap("materials.name"),
 							material }, new Object[] { cb.wrap("vendors.name"),
 							vendor }));
 		else
 			vms = getHibernateSupport().get(
 					cb.getCriteria(),
 					EQ.restrict(new Object[] { cb.wrap("materials.name"),
 							material }));
 
 		if (vms.isEmpty()) {
 			VendorMaterials newVM = new VendorMaterials();
 			if (vendor != null) {
 				CriteriaBuilder cb1 = new CriteriaBuilder(Vendors.class);
 				List<Vendors> vendors = getHibernateSupport().get(
 						cb1.getCriteria(),
 						EQ.restrict(new Object[] { cb.wrap("name"), vendor }));
 				if (!vendors.isEmpty())
 					newVM.setVendors(vendors.get(0));
 			}
 			if (material != null) {
 				CriteriaBuilder cb1 = new CriteriaBuilder(Materials.class);
 				List<Materials> materials = getHibernateSupport()
 						.get(cb1.getCriteria(),
 								EQ.restrict(new Object[] { cb.wrap("name"),
 										material }));
 				if (!materials.isEmpty())
 					newVM.setMaterials(materials.get(0));
 			}
 			if (newVM.getVendors() != null || newVM.getMaterials() != null)
 				newVM.setData("newVendorMaterial");
 			return newVM;
 		}
 
 		Map<Integer, VendorMaterials> vIds = new HashMap<Integer, VendorMaterials>();
 		for (VendorMaterials vm : vms)
 			vIds.put(vm.getVendors().getVendorId(), vm);
 		cb = new CriteriaBuilder(Ranks.class);
 		List<Ranks> ranks = getHibernateSupport()
 				.get(cb.getCriteria(),
 						ASC.order("rank"),
 						IN.restrict(new Object[] { cb.wrap("vendors.id"),
 								vIds.keySet() }));
 		if (ranks.isEmpty())
 			return vms.get(0);
 		return vIds.get(ranks.get(0).getVendors().getVendorId());
 	}
 
 	@Override
 	public PurchaseOrders fetch(Integer poId) {
 		PurchaseOrders po = getHibernateSupport().get(PurchaseOrders.class,
 				poId);
 		return po;
 	}
 }
