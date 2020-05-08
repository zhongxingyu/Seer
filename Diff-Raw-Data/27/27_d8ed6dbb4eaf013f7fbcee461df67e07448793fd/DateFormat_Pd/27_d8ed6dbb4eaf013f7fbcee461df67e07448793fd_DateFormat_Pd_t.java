 package net.nan21.dnet.module.ad.presenter.ext.system.delegate;
 
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 
 import net.nan21.dnet.core.api.enums.DateFormatAttribute;
 import net.nan21.dnet.core.presenter.service.AbstractPresenterDelegate;
 import net.nan21.dnet.module.ad.business.api.system.IDateFormatMaskService;
 import net.nan21.dnet.module.ad.domain.impl.system.DateFormat;
 import net.nan21.dnet.module.ad.domain.impl.system.DateFormatMask;
 import net.nan21.dnet.module.ad.presenter.impl.system.model.DateFormat_Ds;
 
 public class DateFormat_Pd extends AbstractPresenterDelegate {
 
 	/**
 	 * Remove obsolete masks and add the missing ones to the specified
 	 * date-format.
 	 * 
 	 * @param ds
 	 * @throws Exception
 	 */
 	public void synchronizeMasks(DateFormat_Ds ds) throws Exception {
 
 		IDateFormatMaskService srv = (IDateFormatMaskService) this
 				.findEntityService(DateFormatMask.class);
 		DateFormatAttribute[] dfas = DateFormatAttribute.values();
 		List<DateFormatMask> masks = srv.findByDateFormatId(ds.getId());
 
 		// delete obsolete
 
 		List<String> namesToDelete = new ArrayList<String>();
 		for (DateFormatMask mask : masks) {
 			String name = mask.getMask();
 			if (!this.existsMask(dfas, name)) {
 				namesToDelete.add(name);
 			}
 		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("masks", namesToDelete);
		params.put("dateFormatId", ds.getId());

		srv.update("delete from " + DateFormatMask.class.getSimpleName()
				+ " where mask in :masks and dateFormat.id = :dateFormatId",
				params);

		// srv.getEntityManager()
		// .createQuery(
		// "delete from "
		// + DateFormatMask.class.getSimpleName()
		// + " where mask in :masks and dateFormat.id = :dateFormatId")
		// .setParameter("masks", namesToDelete)
		// .setParameter("dateFormatId", ds.getId()).executeUpdate();
 
 		// add missing
 
 		List<DateFormatMask> result = new ArrayList<DateFormatMask>();
 		DateFormat df = srv.findById(ds.getId(), DateFormat.class);
 		for (DateFormatAttribute a : dfas) {
 			if (!existsMask(masks, a.name())) {
 				DateFormatMask attr = new DateFormatMask();
 				attr.setMask(a.name());
 				attr.setDateFormat(df);
 				attr.setValue(this.getSettings().get(a.getPropertyFileKey()));
 				result.add(attr);
 			}
 		}
 		srv.insert(result);
 	}
 
 	private boolean existsMask(DateFormatAttribute[] dfas, String mask) {
 		int l = dfas.length;
 		for (int i = 0; i < l; i++) {
 			if (dfas[i].name().equals(mask)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean existsMask(List<DateFormatMask> list, String mask) {
 		for (DateFormatMask m : list) {
 			if (m.getMask().equals(mask)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
