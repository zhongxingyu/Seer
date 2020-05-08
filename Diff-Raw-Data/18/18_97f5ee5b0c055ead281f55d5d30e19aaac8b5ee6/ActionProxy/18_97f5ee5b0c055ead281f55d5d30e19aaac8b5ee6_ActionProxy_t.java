 package tjuhot.solapop;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.RandomAccess;
 
 public class ActionProxy {
 	List<Rat> m_rat;
 	List<__AUX_ActionHelper> m_helpers;
 
 	public ActionProxy(List<Rat> r) {
 		m_rat = r;
 		m_helpers = new ArrayList<__AUX_ActionHelper>();
 	}
 
 	public void step() {
 		for (int i = 0; i < m_helpers.size(); ++i) {
 			__AUX_ActionHelper hp = m_helpers.get(i);
 			Rat r = m_rat.get(hp.index);
 			if (hp.step_count < 0) {
				r.setHit();
				r.showNext();
 				++hp.step_count;
 			} else if (r.isHole()) {
 
 			} else if (r.isHitted()) {
 				r.showNext();
 				hp.step_count = -10;
 			} else if (!r.isHighest()) {
 				r.showNext();
 			} else {
 				++hp.step_count;
 				if (hp.step_count >= hp.step_high) {
 					r.showNext();
 				}
 			}
 		}
 		Iterator<__AUX_ActionHelper> it = m_helpers.iterator();
 		while (it.hasNext()) {
 			if (m_rat.get((it.next().index)).isHole()) {
 				it.remove();
 			}
 		}
 	}
 
 	public void showRat(int step_high) {
 		int index = 0;
 		List<Integer> avaliableIndex = new ArrayList<Integer>();
 		for (; index < m_rat.size(); ++index) {
 			boolean flag = true;
 			for (__AUX_ActionHelper h : m_helpers) {
 				if (h.index == index) {
 					flag = false;
 					break;
 				}
 			}
 			if (flag == true) {
 				avaliableIndex.add(index);
 			}
 		}
 		index = (int) (Math.random() * (avaliableIndex.size()));
 		if (index == m_rat.size()) {
 			return;
 		} else {
 			__AUX_ActionHelper hp = new __AUX_ActionHelper();
 			hp.step_high = step_high;
 			hp.index = index;
 			m_helpers.add(hp);
 			m_rat.get(index).showNext();
 		}
 	}
 
 	class __AUX_ActionHelper {
 		public int step_high;
 		public int index;
 		public int step_count;
 
 		public __AUX_ActionHelper() {
 			step_count = 0;
 		}
 	}
 
 }
