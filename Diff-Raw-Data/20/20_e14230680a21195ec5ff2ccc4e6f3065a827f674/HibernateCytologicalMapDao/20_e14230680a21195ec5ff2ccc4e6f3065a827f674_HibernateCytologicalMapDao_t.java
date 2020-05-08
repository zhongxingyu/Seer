 /*
$Revision: 1.3 $
$Date: 2006-12-21 19:20:05 $
 
 The Web CGH Software License, Version 1.0
 
 Copyright 2003 RTI. This software was developed in conjunction with the
 National Cancer Institute, and so to the extent government employees are
 co-authors, any rights in such works shall be subject to Title 17 of the
 United States Code, section 105.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this 
 list of conditions and the disclaimer of Article 3, below. Redistributions in 
 binary form must reproduce the above copyright notice, this list of conditions 
 and the following disclaimer in the documentation and/or other materials 
 provided with the distribution.
 
 2. The end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment:
 
 "This product includes software developed by the RTI and the National Cancer 
 Institute."
 
 If no such end-user documentation is to be included, this acknowledgment shall 
 appear in the software itself, wherever such third-party acknowledgments 
 normally appear.
 
 3. The names "The National Cancer Institute", "NCI", 
 Research Triangle Institute, and "RTI" must not be used to endorse or promote 
 products derived from this software.
 
 4. This license does not authorize the incorporation of this software into any 
 proprietary programs. This license does not authorize the recipient to use any 
 trademarks owned by either NCI or RTI.
 
 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE
 NATIONAL CANCER INSTITUTE, RTI, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package org.rti.webcgh.service.dao.hibernate;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
import java.lang.IllegalArgumentException;
 
 import org.rti.webcgh.domain.CytologicalMap;
 import org.rti.webcgh.domain.Organism;
 import org.rti.webcgh.service.dao.CytologicalMapDao;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 /**
  * Implementation of <code>CytologicalMapDao</code> using
  * Hibernate.
  * @author dhall
  *
  */
 public final class HibernateCytologicalMapDao
 extends HibernateDaoSupport implements CytologicalMapDao {
 
 	
 	/**
 	 * Save given map to persistent storage.
 	 * @param cytologicalMap Cytological map.
 	 */
 	public void save(final CytologicalMap cytologicalMap) {
        if ( cytologicalMap == null )
            throw new IllegalArgumentException ( "cytologicalMap parameter cannot be null" ) ;
 		this.getHibernateTemplate().save(cytologicalMap);
 	}
 	
 	/**
 	 * Load cytological map from persistent storage.
 	 * @param id Primary key identifier of
 	 * cytological map.
 	 * @return Cytological map corresponding to
 	 * given id.  Throws an exception if a cytological
 	 * map is not available with given ID.
 	 */
 	public CytologicalMap load(final Long id) {
 		return (CytologicalMap)
 			this.getHibernateTemplate().load(CytologicalMap.class, id);
 	}
 	
 	/**
 	 * Load cytological map from persistent storage.
 	 * @param organism An organism
 	 * @param chromosome Chromosome number
 	 * @return A cytological map or null if there
 	 * is no map from given organism and chromosome.
 	 */
 	public CytologicalMap load(final Organism organism,
 			final short chromosome) {
        
        if ( organism == null )
            throw new IllegalArgumentException ( "Organism parameter cannot be null." ) ;

 		String query = "from CytologicalMap m "
 			+ "where m.organism = ? and m.chromosome = ?";
 		Object[] args = new Object[] {organism, chromosome};
 		List maps = this.getHibernateTemplate().find(query, args);
 		CytologicalMap map = null;
 		if (maps.size() > 0) {
 			map = (CytologicalMap) maps.get(0);
 		}
 		return map;
 	}
 
 	/**
 	 * Delete given cytological map from persistent storage.
 	 * @param cytologicalMap Cytological map to delete.
 	 */
 	public void delete(final CytologicalMap cytologicalMap) {
        if ( cytologicalMap == null )
            throw new IllegalArgumentException ( "cytologicalMap parameter cannot be null" ) ;
 		this.getHibernateTemplate().delete(cytologicalMap);
 	}
 	
 	
 	/**
 	 * Load all cytological maps.
 	 * @return All cytological maps.
 	 */
 	public List<CytologicalMap> loadAll() {
 		List all = this.getHibernateTemplate().loadAll(CytologicalMap.class);
 		List<CytologicalMap> checked = new ArrayList<CytologicalMap>();
 		for (Iterator it = all.iterator(); it.hasNext();) {
 			checked.add((CytologicalMap) it.next());
 		}
 		return checked;
 	}
 	
 	
 	/**
 	 * Delete all cytological maps from given organism.
 	 * @param organism Organism.
 	 */
 	public void deleteAll(final Organism organism) {
        if ( organism == null )
            throw new IllegalArgumentException ( "organism parameter cannot be null" ) ;
 		String query = "from CytologicalMap m where m.organism = ?";
 		Object[] args = new Object[] {organism};
 		List maps = this.getHibernateTemplate().find(query, args);
 		this.getHibernateTemplate().deleteAll(maps);
 	}
 }
