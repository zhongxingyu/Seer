 package org.cimmyt.cril.ibwb.api;
 
 import ibfb.domain.core.Measurement;
 import java.sql.ResultSet;
 import java.util.List;
 //import javax.faces.view.Location;
 import org.cimmyt.cril.ibwb.domain.*;
 import org.cimmyt.cril.ibwb.domain.constants.TypeDB;
 import org.cimmyt.cril.ibwb.domain.inventory.InventoryData;
 import org.cimmyt.cril.ibwb.domain.util.WheatData;
 
 /**
  *
  * @author jgcamarena
  */
 public interface CommonServices {
 
     //-----------------------------------Atributs---------------------------
     /**
      * Adds an Object Atributs to database
      *
      * @param atributs Objeto a agregar
      */
     public void addAtributs(Atributs atributs);
 
     /**
      * Updates a record of type Atributs in database
      *
      * @param atributs Objeto a actualizar
      */
     public void updateAtributs(Atributs atributs);
 
     /**
      * Deletes an object Atributs from database
      *
      * @param atributs Objeto a eliminar
      */
     public void deleteAtributs(Atributs atributs);
 
     /**
      * Gets an Object from database Atributs of the type Atributs
      *
      * @param atributs
      * @return Atributs
      */
     public Atributs getAtributs(Atributs atributs);
 
     /**
      * Gets an Object of type Atributs Finding the record by its ID Atributs in
      * String format
      *
      * @param idAtributs
      * @return Atributs
      */
     public Atributs getAtributs(Integer idAtributs);
 
     /**
      * Gets a list of Objects Atributs
      *
      * @return List
      */
     public List<Atributs> getAtributsList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param atributsFiltro Object to count total items
      */
     public int getTotalAtributs(Atributs atributsFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param atributsFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Atributs> getListAtributs(Atributs atributsFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Bibrefs---------------------------
     /**
      * Adds an Object Bibrefs to database
      *
      * @param bibrefs Objeto a agregar
      */
     public void addBibrefs(Bibrefs bibrefs);
 
     /**
      * Updates a record of type Bibrefs in database
      *
      * @param bibrefs Objeto a actualizar
      */
     public void updateBibrefs(Bibrefs bibrefs);
 
     /**
      * Deletes an object Bibrefs from database
      *
      * @param bibrefs Objeto a eliminar
      */
     public void deleteBibrefs(Bibrefs bibrefs);
 
     /**
      * Gets an Object from database Bibrefs of the type Bibrefs
      *
      * @param bibrefs
      * @return Bibrefs
      */
     public Bibrefs getBibrefs(Bibrefs bibrefs);
 
     /**
      * Gets an Object of type Bibrefs Finding the record by its ID Bibrefs in
      * String format
      *
      * @param idBibrefs
      * @return Bibrefs
      */
     public Bibrefs getBibrefs(Integer idBibrefs);
 
     /**
      * Gets a list of Objects Bibrefs
      *
      * @return List
      */
     public List<Bibrefs> getBibrefsList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param bibrefsFiltro Object to count total items
      */
     public int getTotalBibrefs(Bibrefs bibrefsFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param bibrefsFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Bibrefs> getListBibrefs(Bibrefs bibrefsFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Changes---------------------------
     /**
      * Adds an Object Changes to database
      *
      * @param changes Objeto a agregar
      */
     public void addChanges(Changes changes);
 
     /**
      * Updates a record of type Changes in database
      *
      * @param changes Objeto a actualizar
      */
     public void updateChanges(Changes changes);
 
     /**
      * Deletes an object Changes from database
      *
      * @param changes Objeto a eliminar
      */
     public void deleteChanges(Changes changes);
 
     /**
      * Gets an Object from database Changes of the type Changes
      *
      * @param changes
      * @return Changes
      */
     public Changes getChanges(Changes changes);
 
     /**
      * Gets an Object of type Changes Finding the record by its ID Changes in
      * String format
      *
      * @param idChanges
      * @return Changes
      */
     public Changes getChanges(Integer idChanges);
 
     /**
      * Gets a list of Objects Changes
      *
      * @return List
      */
     public List<Changes> getChangesList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param changesFiltro Object to count total items
      */
     public int getTotalChanges(Changes changesFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param changesFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Changes> getListChanges(Changes changesFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Cntry---------------------------
     /**
      * Adds an Object Cntry to database
      *
      * @param cntry Objeto a agregar
      */
     public void addCntry(Cntry cntry);
 
     /**
      * Updates a record of type Cntry in database
      *
      * @param cntry Objeto a actualizar
      */
     public void updateCntry(Cntry cntry);
 
     /**
      * Deletes an object Cntry from database
      *
      * @param cntry Objeto a eliminar
      */
     public void deleteCntry(Cntry cntry);
 
     /**
      * Gets an Object from database Cntry of the type Cntry
      *
      * @param cntry
      * @return Cntry
      */
     public Cntry getCntry(Cntry cntry);
 
     /**
      * Gets an Object of type Cntry Finding the record by its ID Cntry in String
      * format
      *
      * @param idCntry
      * @return Cntry
      */
     public Cntry getCntry(Integer idCntry);
 
     /**
      * Gets a list of Objects Cntry
      *
      * @return List
      */
     public List<Cntry> getCntryList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param cntryFiltro Object to count total items
      */
     public int getTotalCntry(Cntry cntryFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param cntryFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Cntry> getListCntry(Cntry cntryFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------DataC---------------------------
     /**
      * Adds an Object DataC to database
      *
      * @param dataC Objeto a agregar
      */
     public void addDataC(DataC dataC);
 
     /**
      * Adds or updates an Object DataC to database
      *
      * @param dataC Objeto a agregar
      */
     public void addOrUpdateDataC(DataC dataC);
 
     /**
      * Updates a record of type DataC in database
      *
      * @param dataC Objeto a actualizar
      */
     public void updateDataC(DataC dataC);
 
     /**
      * Deletes an object DataC from database
      *
      * @param dataC Objeto a eliminar
      */
     public void deleteDataC(DataC dataC);
 
     /**
      * Gets an Object from database DataC of the type DataC
      *
      * @param dataC
      * @return DataC
      */
 //    public DataC getDataC(DataC dataC);
     /**
      * Gets an Object of type DataC Finding the record by its ID DataC in String
      * format
      *
      * @param idDataC
      * @return DataC
      */
 //    public DataC getDataC(Integer idDataC);
     /**
      * Gets a list of Objects DataC
      *
      * @return List
      */
     public List<DataC> getDataCList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param dataCFiltro Object to count total items
      */
     public int getTotalDataC(DataC dataCFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param dataCFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<DataC> getListDataC(DataC dataCFilter, int start, int pageSize, boolean paged);
 
     /**
      * Retrieve a list of DATA_n records by its Effect ID
      *
      * @param effectId
      * @return
      */
     public List<DataC> getDataCByEffectId(final Integer effectId);
 
     //-----------------------------------DataN---------------------------
     /**
      * Adds an Object DataN to database
      *
      * @param dataN Objeto a agregar
      */
     public void addDataN(DataN dataN);
 
     /**
      * Adds or Update an Object DataN to database
      *
      * @param dataN Objeto a agregar
      */
     public void addOrUpdateDataN(DataN dataN);
 
     /**
      * Updates a record of type DataN in database
      *
      * @param dataN Objeto a actualizar
      */
     public void updateDataN(DataN dataN);
 
     /**
      * Deletes an object DataN from database
      *
      * @param dataN Objeto a eliminar
      */
     public void deleteDataN(DataN dataN);
 
     /**
      * Gets an Object from database DataN of the type DataN
      *
      * @param dataN
      * @return DataN
      */
 //    public DataN getDataN(DataN dataN);
     /**
      * Gets an Object of type DataN Finding the record by its ID DataN in String
      * format
      *
      * @param idDataN
      * @return DataN
      */
 //    public DataN getDataN(Integer idDataN);
     /**
      * Gets a list of Objects DataN
      *
      * @return List
      */
     public List<DataN> getDataNList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param dataNFiltro Object to count total items
      */
     public int getTotalDataN(DataN dataNFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param dataNFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<DataN> getListDataN(DataN dataNFilter, int start, int pageSize, boolean paged);
 
     /**
      * Retrieve a list of DATA_n records by its Effect ID
      *
      * @param effectId
      * @return
      */
     public List<DataN> getDataNByEffectId(final Integer effectId);
 
     //-----------------------------------DataT---------------------------
     /**
      * Adds an Object DataT to database
      *
      * @param dataT Objeto a agregar
      */
     public void addDataT(DataT dataT);
 
     /**
      * Updates a record of type DataT in database
      *
      * @param dataT Objeto a actualizar
      */
     public void updateDataT(DataT dataT);
 
     /**
      * Deletes an object DataT from database
      *
      * @param dataT Objeto a eliminar
      */
     public void deleteDataT(DataT dataT);
 
     /**
      * Gets an Object from database DataT of the type DataT
      *
      * @param dataT
      * @return DataT
      */
 //    public DataT getDataT(DataT dataT);
     /**
      * Gets an Object of type DataT Finding the record by its ID DataT in String
      * format
      *
      * @param idDataT
      * @return DataT
      */
 //    public DataT getDataT(Integer idDataT);
     /**
      * Gets a list of Objects DataT
      *
      * @return List
      */
     public List<DataT> getDataTList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param dataTFiltro Object to count total items
      */
     public int getTotalDataT(DataT dataTFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param dataTFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<DataT> getListDataT(DataT dataTFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Datattr---------------------------
     /**
      * Adds an Object Datattr to database
      *
      * @param datattr Objeto a agregar
      */
     public void addDatattr(Datattr datattr);
 
     /**
      * Updates a record of type Datattr in database
      *
      * @param datattr Objeto a actualizar
      */
     public void updateDatattr(Datattr datattr);
 
     /**
      * Deletes an object Datattr from database
      *
      * @param datattr Objeto a eliminar
      */
     public void deleteDatattr(Datattr datattr);
 
     /**
      * Gets an Object from database Datattr of the type Datattr
      *
      * @param datattr
      * @return Datattr
      */
     public Datattr getDatattr(Datattr datattr);
 
     /**
      * Gets an Object of type Datattr Finding the record by its ID Datattr in
      * String format
      *
      * @param idDatattr
      * @return Datattr
      */
     public Datattr getDatattr(Integer idDatattr);
 
     /**
      * Gets a list of Objects Datattr
      *
      * @return List
      */
     public List<Datattr> getDatattrList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param datattrFiltro Object to count total items
      */
     public int getTotalDatattr(Datattr datattrFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param datattrFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Datattr> getListDatattr(Datattr datattrFilter, int start, int pageSize, boolean paged);
 
 //    //-----------------------------------Dmsattr---------------------------
 //    /**
 //     * Adds an Object Dmsattr to database
 //     *
 //     * @param dmsattr Objeto a agregar
 //     */
 //    public void addDmsattr(Dmsattr dmsattr);
 //
 //    /**
 //     * Updates a record of type Dmsattr in database
 //     *
 //     * @param dmsattr Objeto a actualizar
 //     */
 //    public void updateDmsattr(Dmsattr dmsattr);
 //
 //    /**
 //     * Deletes an object Dmsattr from database
 //     *
 //     * @param dmsattr Objeto a eliminar
 //     */
 //    public void deleteDmsattr(Dmsattr dmsattr);
 //
 //    /**
 //     * Gets an Object from database Dmsattr of the type Dmsattr
 //     *
 //     * @param dmsattr
 //     * @return Dmsattr
 //     */
 //    public Dmsattr getDmsattr(Dmsattr dmsattr);
 //
 //    /**
 //     * Gets an Object of type Dmsattr Finding the record by its ID Dmsattr in
 //     * String format
 //     *
 //     * @param idDmsattr
 //     * @return Dmsattr
 //     */
 //    public Dmsattr getDmsattr(Integer idDmsattr);
 //
 //    /**
 //     * Gets an Object of type Dmsattr by Dmsatrec And Dmsatype
 //     *
 //     * @param dmsattr
 //     * @return Dmsattr
 //     */
 //    public Dmsattr getDmsattrByDmsatrecAndDmsatype(Dmsattr dmsattr);
 //
 //    /**
 //     * Gets a list of Objects Dmsattr
 //     *
 //     * @return List
 //     */
 //    public List<Dmsattr> getDmsattrList();
 //
 //    /**
 //     * Gets the number of records matching with filter
 //     *
 //     * @param dmsattrFiltro Object to count total items
 //     */
 //    public int getTotalDmsattr(Dmsattr dmsattrFilter);
 //
 //    /**
 //     * Gets a list of Objects for pagination
 //     *
 //     * @param dmsattrFiltro	The filter object
 //     * @param inicio initial record
 //     * @param tamanioPagina page size
 //     * @return List
 //     */
 //    public List<Dmsattr> getListDmsattr(Dmsattr dmsattrFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Dudflds---------------------------
     /**
      * Adds an Object Dudflds to database
      *
      * @param dudflds Objeto a agregar
      */
     public void addDudflds(Dudflds dudflds);
 
     /**
      * Updates a record of type Dudflds in database
      *
      * @param dudflds Objeto a actualizar
      */
     public void updateDudflds(Dudflds dudflds);
 
     /**
      * Deletes an object Dudflds from database
      *
      * @param dudflds Objeto a eliminar
      */
     public void deleteDudflds(Dudflds dudflds);
 
     /**
      * Gets an Object from database Dudflds of the type Dudflds
      *
      * @param dudflds
      * @return Dudflds
      */
     public Dudflds getDudflds(Dudflds dudflds);
 
     /**
      * Gets an Object of type Dudflds Finding the record by its ID Dudflds in
      * String format
      *
      * @param idDudflds
      * @return Dudflds
      */
     public Dudflds getDudflds(Integer idDudflds);
 
     /**
      * Gets a list of Objects Dudflds
      *
      * @return List
      */
     public List<Dudflds> getDudfldsList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param dudfldsFiltro Object to count total items
      */
     public int getTotalDudflds(Dudflds dudfldsFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param dudfldsFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Dudflds> getListDudflds(Dudflds dudfldsFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Effect---------------------------
     /**
      * Adds an Object Effect to database
      *
      * @param effect Objeto a agregar
      */
     public void addEffect(Effect effect);
 
     /**
      * Updates a record of type Effect in database
      *
      * @param effect Objeto a actualizar
      */
     public void updateEffect(Effect effect);
 
     /**
      * Deletes an object Effect from database
      *
      * @param effect Objeto a eliminar
      */
     public void deleteEffect(Effect effect);
 
     /**
      * Gets an Object from database Effect of the type Effect
      *
      * @param effect
      * @return Effect
      */
 //    public Effect getEffect(Effect effect);
     /**
      * Gets an Object of type Effect Finding the record by its ID Effect in
      * String format
      *
      * @param idEffect
      * @return Effect
      */
 //    public Effect getEffect(Integer idEffect);
     /**
      * Gets a list of Objects Effect
      *
      * @return List
      */
     public List<Effect> getEffectList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param effectFiltro Object to count total items
      */
     public int getTotalEffect(Effect effectFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param effectFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Effect> getListEffect(Effect effectFilter, int start, int pageSize, boolean paged);
 
     public List<Effect> getEffectsByEffectsids(final List effectsIds);
 
     //-----------------------------------Factor---------------------------
     /**
      * Adds an Object Factor to database
      *
      * @param factor Objeto a agregar
      */
     public void addFactor(Factor factor);
 
     /**
      * Updates a record of type Factor in database
      *
      * @param factor Objeto a actualizar
      */
     public void updateFactor(Factor factor);
 
     /**
      * Deletes an object Factor from database
      *
      * @param factor Objeto a eliminar
      */
     public void deleteFactor(Factor factor);
 
     /**
      * Gets an Object from database Factor of the type Factor
      *
      * @param factor
      * @return Factor
      */
     public Factor getFactor(Factor factor);
 
     /**
      * Gets an Object of type Factor Finding the record by its ID Factor in
      * String format
      *
      * @param idFactor
      * @return Factor
      */
     public Factor getFactor(Integer idFactor);
 
     /**
      * Gets a list of Objects Factor
      *
      * @return List
      */
     public List<Factor> getFactorList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param factorFiltro Object to count total items
      */
     public int getTotalFactor(Factor factorFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param factorFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Factor> getListFactor(Factor factorFilter, int start, int pageSize, boolean paged);
 
     /**
      * Return a list of grouping factors by study id
      *
      * @param studyid ID for study
      * @return list of factor or empty list if study id not found
      */
     public List<Factor> getFactorsForStudy(final Integer studyid);
 
     /**
      * Returns different combinations of TRAIT, SCALE and METHOD
      *
      * @return List<Factor>
      */
     public List<Factor> getFactorConvinacionesTraitScaleMethod();
 
     /**
      * Return the main factors by studyid
      *
      * @param Integer studyid
      * @return List<Factor>
      */
     public List<Factor> getMainFactorsByStudyid(Integer studyid);
 
     /**
      * Return the group of the main factors by studyid and factorid
      *
      * @param Integer studyid
      * @return List<Factor>
      */
     public List<Factor> getGroupFactorsByStudyidAndFactorid(Integer studyid, Integer factorid);
 
     public List<Factor> getFactorsByFactorsids(List factorIds);
     
     public Factor getFactorByStudyidAndFname(Integer studyid, String fname);
 
     //-----------------------------------Georef---------------------------
     /**
      * Adds an Object Georef to database
      *
      * @param georef Objeto a agregar
      */
     public void addGeoref(Georef georef);
 
     /**
      * Updates a record of type Georef in database
      *
      * @param georef Objeto a actualizar
      */
     public void updateGeoref(Georef georef);
 
     /**
      * Deletes an object Georef from database
      *
      * @param georef Objeto a eliminar
      */
     public void deleteGeoref(Georef georef);
 
     /**
      * Gets an Object from database Georef of the type Georef
      *
      * @param georef
      * @return Georef
      */
     public Georef getGeoref(Georef georef);
 
     /**
      * Gets an Object of type Georef Finding the record by its ID Georef in
      * String format
      *
      * @param idGeoref
      * @return Georef
      */
     public Georef getGeoref(Integer idGeoref);
 
     /**
      * Gets a list of Objects Georef
      *
      * @return List
      */
     public List<Georef> getGeorefList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param georefFiltro Object to count total items
      */
     public int getTotalGeoref(Georef georefFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param georefFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Georef> getListGeoref(Georef georefFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Germplsm---------------------------
     /**
      * Adds an Object Germplsm to database
      *
      * @param germplsm Objeto a agregar
      */
     public void addGermplsm(Germplsm germplsm);
 
     /**
      * Updates a record of type Germplsm in database
      *
      * @param germplsm Objeto a actualizar
      */
     public void updateGermplsm(Germplsm germplsm);
 
     /**
      * Deletes an object Germplsm from database
      *
      * @param germplsm Objeto a eliminar
      */
     public void deleteGermplsm(Germplsm germplsm);
 
     /**
      * Gets an Object from database Germplsm of the type Germplsm
      *
      * @param germplsm
      * @return Germplsm
      */
     public Germplsm getGermplsm(Germplsm germplsm);
 
     /**
      * Gets an Object of type Germplsm Finding the record by its ID Germplsm in
      * String format
      *
      * @param idGermplsm
      * @return Germplsm
      */
     public Germplsm getGermplsm(Integer idGermplsm);
 
     /**
      * Gets a list of Objects Germplsm
      *
      * @return List
      */
     public List<Germplsm> getGermplsmList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param germplsmFiltro Object to count total items
      */
     public int getTotalGermplsm(Germplsm germplsmFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param germplsmFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Germplsm> getListGermplsm(Germplsm germplsmFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Institut---------------------------
     /**
      * Adds an Object Institut to database
      *
      * @param institut Objeto a agregar
      */
     public void addInstitut(Institut institut);
 
     /**
      * Updates a record of type Institut in database
      *
      * @param institut Objeto a actualizar
      */
     public void updateInstitut(Institut institut);
 
     /**
      * Deletes an object Institut from database
      *
      * @param institut Objeto a eliminar
      */
     public void deleteInstitut(Institut institut);
 
     /**
      * Gets an Object from database Institut of the type Institut
      *
      * @param institut
      * @return Institut
      */
     public Institut getInstitut(Institut institut);
 
     /**
      * Gets an Object of type Institut Finding the record by its ID Institut in
      * String format
      *
      * @param idInstitut
      * @return Institut
      */
     public Institut getInstitut(Integer idInstitut);
 
     /**
      * Gets a list of Objects Institut
      *
      * @return List
      */
     public List<Institut> getInstitutList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param institutFiltro Object to count total items
      */
     public int getTotalInstitut(Institut institutFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param institutFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Institut> getListInstitut(Institut institutFilter, int start, int pageSize, boolean paged);
 
     /**
      * Gets a list of Institutions by city name
      *
      * @param city name of the city to retrieve
      * @return a list of
      * <code>Institut</code> of empty list if there are not records
      */
     public List<Institut> getInstitutionsByCity(final String city);
 
     //-----------------------------------Instln---------------------------
     /**
      * Adds an Object Instln to database
      *
      * @param instln Objeto a agregar
      */
     public void addInstln(Instln instln);
 
     /**
      * Updates a record of type Instln in database
      *
      * @param instln Objeto a actualizar
      */
     public void updateInstln(Instln instln);
 
     /**
      * Deletes an object Instln from database
      *
      * @param instln Objeto a eliminar
      */
     public void deleteInstln(Instln instln);
 
     /**
      * Gets an Object from database Instln of the type Instln
      *
      * @param instln
      * @return Instln
      */
     public Instln getInstln(Instln instln);
 
     /**
      * Gets an Object of type Instln Finding the record by its ID Instln in
      * String format
      *
      * @param idInstln
      * @return Instln
      */
     public Instln getInstln(Integer idInstln);
 
     /**
      * Gets a list of Objects Instln
      *
      * @return List
      */
     public List<Instln> getInstlnList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param instlnFiltro Object to count total items
      */
     public int getTotalInstln(Instln instlnFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param instlnFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Instln> getListInstln(Instln instlnFilter, int start, int pageSize, boolean paged);
 
     public List<LevelN> getLevelnByLabelid(Integer labelid);
 
     //-----------------------------------LevelC---------------------------
     /**
      * Adds an Object LevelC to database
      *
      * @param levelC Objeto a agregar
      */
     public void addLevelC(LevelC levelC);
 
     /**
      * Updates a record of type LevelC in database
      *
      * @param levelC Objeto a actualizar
      */
     public void updateLevelC(LevelC levelC);
 
     /**
      * Deletes an object LevelC from database
      *
      * @param levelC Objeto a eliminar
      */
     public void deleteLevelC(LevelC levelC);
 
     /**
      * Gets an Object from database LevelC of the type LevelC
      *
      * @param levelC
      * @return LevelC
      */
     public LevelC getLevelC(LevelC levelC);
 
     /**
      * Gets an Object of type LevelC Finding the record by its ID LevelC in
      * String format
      *
      * @param idLevelC
      * @return LevelC
      */
     public LevelC getLevelC(Integer idLevelC);
 
     /**
      * Gets a list of Objects LevelC
      *
      * @return List
      */
     public List<LevelC> getLevelCList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param levelCFiltro Object to count total items
      */
     public int getTotalLevelC(LevelC levelCFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param levelCFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<LevelC> getListLevelC(LevelC levelCFilter, int start, int pageSize, boolean paged);
 
     public List<LevelC> getLevelsCByLabelid(Integer labelid);
 
     //-----------------------------------LevelN---------------------------
     /**
      * Adds an Object LevelN to database
      *
      * @param levelN Objeto a agregar
      */
     public void addLevelN(LevelN levelN);
 
     /**
      * Updates a record of type LevelN in database
      *
      * @param levelN Objeto a actualizar
      */
     public void updateLevelN(LevelN levelN);
 
     /**
      * Deletes an object LevelN from database
      *
      * @param levelN Objeto a eliminar
      */
     public void deleteLevelN(LevelN levelN);
 
     /**
      * Gets an Object from database LevelN of the type LevelN
      *
      * @param levelN
      * @return LevelN
      */
 //    public LevelN getLevelN(LevelN levelN);
     /**
      * Gets an Object of type LevelN Finding the record by its ID LevelN in
      * String format
      *
      * @param idLevelN
      * @return LevelN
      */
 //    public LevelN getLevelN(Integer idLevelN);
     /**
      * Gets a list of Objects LevelN
      *
      * @return List
      */
     public List<LevelN> getLevelNList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param levelNFiltro Object to count total items
      */
     public int getTotalLevelN(LevelN levelNFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param levelNFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<LevelN> getListLevelN(LevelN levelNFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------LevelT---------------------------
     /**
      * Adds an Object LevelT to database
      *
      * @param levelT Objeto a agregar
      */
     public void addLevelT(LevelT levelT);
 
     /**
      * Updates a record of type LevelT in database
      *
      * @param levelT Objeto a actualizar
      */
     public void updateLevelT(LevelT levelT);
 
     /**
      * Deletes an object LevelT from database
      *
      * @param levelT Objeto a eliminar
      */
     public void deleteLevelT(LevelT levelT);
 
     /**
      * Gets an Object from database LevelT of the type LevelT
      *
      * @param levelT
      * @return LevelT
      */
 //    public LevelT getLevelT(LevelT levelT);
     /**
      * Gets an Object of type LevelT Finding the record by its ID LevelT in
      * String format
      *
      * @param idLevelT
      * @return LevelT
      */
 //    public LevelT getLevelT(Integer idLevelT);
     /**
      * Gets a list of Objects LevelT
      *
      * @return List
      */
     public List<LevelT> getLevelTList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param levelTFiltro Object to count total items
      */
     public int getTotalLevelT(LevelT levelTFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param levelTFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<LevelT> getListLevelT(LevelT levelTFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Levels---------------------------
     /**
      * Adds an Object Levels to database
      *
      * @param levels Objeto a agregar
      */
     public void addLevels(Levels levels);
 
     /**
      * Updates a record of type Levels in database
      *
      * @param levels Objeto a actualizar
      */
     public void updateLevels(Levels levels);
 
     /**
      * Deletes an object Levels from database
      *
      * @param levels Objeto a eliminar
      */
     public void deleteLevels(Levels levels);
 
     public Integer getNextLevelNo();
 
     /**
      * Gets an Object from database Levels of the type Levels
      *
      * @param levels
      * @return Levels
      */
 //    public Levels getLevels(Levels levels);
     /**
      * Gets an Object of type Levels Finding the record by its ID Levels in
      * String format
      *
      * @param idLevels
      * @return Levels
      */
 //    public Levels getLevels(Integer idLevels);
     /**
      * Gets a list of Objects Levels
      *
      * @return List
      */
     public List<Levels> getLevelsList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param levelsFiltro Object to count total items
      */
     public int getTotalLevels(Levels levelsFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param levelsFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Levels> getListLevels(Levels levelsFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Listdata---------------------------
     /**
      * Adds an Object Listdata to database
      *
      * @param listdata Objeto a agregar
      */
     public void addListdata(Listdata listdata);
 
     /**
      * Updates a record of type Listdata in database
      *
      * @param listdata Objeto a actualizar
      */
     public void updateListdata(Listdata listdata);
 
     /**
      * Deletes an object Listdata from database
      *
      * @param listdata Objeto a eliminar
      */
     public void deleteListdata(Listdata listdata);
 
     /**
      * Gets an Object from database Listdata of the type Listdata
      *
      * @param listdata
      * @return Listdata
      */
 //    public Listdata getListdata(Listdata listdata);
     /**
      * Gets an Object of type Listdata Finding the record by its ID Listdata in
      * String format
      *
      * @param idListdata
      * @return Listdata
      */
 //    public Listdata getListdata(Integer idListdata);
     /**
      * Gets a list of Objects Listdata
      *
      * @return List
      */
     public List<Listdata> getListdataList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param listdataFiltro Object to count total items
      */
     public int getTotalListdata(Listdata listdataFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param listdataFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Listdata> getListListdata(Listdata listdataFilter, int start, int pageSize, boolean paged);
 
     public List<Listdata> getListListdataFiltro(Listdata filter, int start, int pageSize, boolean paged);
     
     public List<Listdata> getListdataByIdlistnms(final Integer idListnms, TypeDB typeDb);
 
     //-----------------------------------Listnms---------------------------
     /**
      * Adds an Object Listnms to database
      *
      * @param listnms Objeto a agregar
      */
     public void addListnms(Listnms listnms);
 
     /**
      * Updates a record of type Listnms in database
      *
      * @param listnms Objeto a actualizar
      */
     public void updateListnms(Listnms listnms);
 
     /**
      * Deletes an object Listnms from database
      *
      * @param listnms Objeto a eliminar
      */
     public void deleteListnms(Listnms listnms);
 
     /**
      * Gets an Object from database Listnms of the type Listnms
      *
      * @param listnms
      * @return Listnms
      */
     public Listnms getListnms(Listnms listnms);
 
     /**
      * Gets an Object of type Listnms Finding the record by its ID Listnms in
      * String format
      *
      * @param idListnms
      * @return Listnms
      */
     public Listnms getListnms(Integer idListnms);
 
     /**
      * Gets a list of Objects Listnms
      *
      * @return List
      */
     public List<Listnms> getListnmsList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param listnmsFiltro Object to count total items
      */
     public int getTotalListnms(Listnms listnmsFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param listnmsFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Listnms> getListListnms(Listnms listnmsFilter, int start, int pageSize, boolean paged);
     
     /**
      * Checks if a List already exists in local
      * @param listName
      * @return 
      */
     public boolean existGermplasmListInLocal(String listName);    
 
     //-----------------------------------Location---------------------------
     /**
      * Adds an Object Location to database
      *
      * @param location Objeto a agregar
      */
     public void addLocation(Location location);
 
     /**
      * Updates a record of type Location in database
      *
      * @param location Objeto a actualizar
      */
     public void updateLocation(Location location);
 
     /**
      * Deletes an object Location from database
      *
      * @param location Objeto a eliminar
      */
     public void deleteLocation(Location location);
 
     /**
      * Gets an Object from database Location of the type Location
      *
      * @param location
      * @return Location
      */
     public Location getLocation(Location location);
 
     /**
      * Gets an Object of type Location Finding the record by its ID Location in
      * String format
      *
      * @param idLocation
      * @return Location
      */
     public Location getLocation(Integer idLocation);
 
     /**
      * Gets a list of Objects Location
      *
      * @return List
      */
     public List<Location> getLocationList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param locationFiltro Object to count total items
      */
     public int getTotalLocation(Location locationFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param locationFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Location> getListLocation(Location locationFilter, int start, int pageSize, boolean paged);
 
     /**
      * Gets a list of locations by country
      *
      * @param countryId Country Id
      * @param fromLocid First location ID
      * @param toLocid Last location ID
      * @return List of locations
      */
     public List<Location> getLocationsByCountryLocidRange(final Integer countryId, final Integer fromLocid, final Integer toLocid);
 
     //-----------------------------------Locdes---------------------------
     /**
      * Adds an Object Locdes to database
      *
      * @param locdes Objeto a agregar
      */
     public void addLocdes(Locdes locdes);
 
     /**
      * Updates a record of type Locdes in database
      *
      * @param locdes Objeto a actualizar
      */
     public void updateLocdes(Locdes locdes);
 
     /**
      * Deletes an object Locdes from database
      *
      * @param locdes Objeto a eliminar
      */
     public void deleteLocdes(Locdes locdes);
 
     /**
      * Gets an Object from database Locdes of the type Locdes
      *
      * @param locdes
      * @return Locdes
      */
     public Locdes getLocdes(Locdes locdes);
 
     /**
      * Gets an Object of type Locdes Finding the record by its ID Locdes in
      * String format
      *
      * @param idLocdes
      * @return Locdes
      */
     public Locdes getLocdes(Integer idLocdes);
 
     /**
      * Gets a list of Objects Locdes
      *
      * @return List
      */
     public List<Locdes> getLocdesList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param locdesFiltro Object to count total items
      */
     public int getTotalLocdes(Locdes locdesFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param locdesFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Locdes> getListLocdes(Locdes locdesFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Measuredin---------------------------
     /**
      * Adds an Object Measuredin to database
      *
      * @param measuredin Objeto a agregar
      */
     public void addMeasuredin(Measuredin measuredin);
 
     /**
      * Updates a record of type Measuredin in database
      *
      * @param measuredin Objeto a actualizar
      */
     public void updateMeasuredin(Measuredin measuredin);
 
     /**
      * Deletes an object Measuredin from database
      *
      * @param measuredin Objeto a eliminar
      */
     public void deleteMeasuredin(Measuredin measuredin);
 
     /**
      * Gets an Object from database Measuredin of the type Measuredin
      *
      * @param measuredin
      * @return Measuredin
      */
     public Measuredin getMeasuredin(Measuredin measuredin);
 
     /**
      * Gets an Object of type Measuredin Finding the record by its ID Measuredin
      * in String format
      *
      * @param idMeasuredin
      * @return Measuredin
      */
     public Measuredin getMeasuredin(Integer idMeasuredin);
 
     /**
      * Gets an Object of type Measuredin Finding the record by its ID Measuredin
      * in String format
      *
      * @param Measuredin
      * @return Measuredin
      */
     public Measuredin getMeasuredinByTraitidScaleidTmethid(Measuredin measuredin);
 
     public Measuredin getMeasuredinByTraitidAndScaleid(Measuredin measuredin);
 
     /**
      * Gets a list of Objects Measuredin
      *
      * @return List
      */
     public List<Measuredin> getMeasuredinList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param measuredinFiltro Object to count total items
      */
     public int getTotalMeasuredin(Measuredin measuredinFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param measuredinFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Measuredin> getListMeasuredin(Measuredin measuredinFilter, int start, int pageSize, boolean paged);
 
     /**
      * Gets a list of measured in list by trait id
      *
      * @param traitId Trait ID
      * @return List of measuredin objects filled with it's scale and method
      */
     public List<Measuredin> getMeasuredInListByTrait(final Integer traitId);
 
     /**
      * Adds or update a Measuredin
      *
      * @param measuredin
      */
     public void addOrUpdateMeasuredIn(Measuredin measuredin);
 
     //-----------------------------------Methods---------------------------
     /**
      * Adds an Object Methods to database
      *
      * @param methods Objeto a agregar
      */
     public void addMethods(Methods methods);
 
     /**
      * Updates a record of type Methods in database
      *
      * @param methods Objeto a actualizar
      */
     public void updateMethods(Methods methods);
 
     /**
      * Deletes an object Methods from database
      *
      * @param methods Objeto a eliminar
      */
     public void deleteMethods(Methods methods);
 
     /**
      * Gets an Object from database Methods of the type Methods
      *
      * @param methods
      * @return Methods
      */
     public Methods getMethods(Methods methods);
 
     /**
      * Gets an Object of type Methods Finding the record by its ID Methods in
      * String format
      *
      * @param idMethods
      * @return Methods
      */
     public Methods getMethods(Integer idMethods);
 
     /**
      * Gets a list of Objects Methods
      *
      * @return List
      */
     public List<Methods> getMethodsList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param methodsFiltro Object to count total items
      */
     public int getTotalMethods(Methods methodsFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param methodsFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Methods> getListMethods(Methods methodsFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Names---------------------------
     /**
      * Adds an Object Names to database
      *
      * @param names Objeto a agregar
      */
     public void addNames(Names names);
 
     /**
      * Updates a record of type Names in database
      *
      * @param names Objeto a actualizar
      */
     public void updateNames(Names names);
 
     /**
      * Deletes an object Names from database
      *
      * @param names Objeto a eliminar
      */
     public void deleteNames(Names names);
 
     /**
      * Gets an Object from database Names of the type Names
      *
      * @param names
      * @return Names
      */
     public Names getNames(Names names);
 
     /**
      * Gets an Object of type Names Finding the record by its ID Names in String
      * format
      *
      * @param idNames
      * @return Names
      */
     public Names getNames(Integer idNames);
 
     /**
      * Gets a list of Objects Names
      *
      * @return List
      */
     public List<Names> getNamesList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param namesFiltro Object to count total items
      */
     public int getTotalNames(Names namesFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param namesFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Names> getListNames(Names namesFilter, int start, int pageSize, boolean paged);
     
     public String getNextMaxForBCID(String cadena, Integer ntype);
     
     public Names getNamesByGid(Germplsm germplasm, Boolean preferido);
     
     public Integer getMaxForSelection(String cadena, Integer ntype);
     
     public Listnms getNamesCentral(final Listnms listnms);
     public Listnms getNamesLocal(final Listnms listnms);
     
     /**
      * Gets a list for Wheat Data (cimmyt) related to BCID, Selection history
      * 1. It looks for all elements in names where gid are used by a list
      * @param listId
      * @return Gets a list for Wheat Data (cimmyt)
      */
     public List<WheatData> getDataForCimmytWheat(final Integer listId);    
     
     //-----------------------------------Obsunit---------------------------
     /**
      * Adds an Object Obsunit to database
      *
      * @param obsunit Objeto a agregar
      */
     public void addObsunit(Obsunit obsunit);
 
     /**
      * Updates a record of type Obsunit in database
      *
      * @param obsunit Objeto a actualizar
      */
     public void updateObsunit(Obsunit obsunit);
 
     /**
      * Deletes an object Obsunit from database
      *
      * @param obsunit Objeto a eliminar
      */
     public void deleteObsunit(Obsunit obsunit);
 
     /**
      * Gets an Object from database Obsunit of the type Obsunit
      *
      * @param obsunit
      * @return Obsunit
      */
     public Obsunit getObsunit(Obsunit obsunit);
 
     /**
      * Gets an Object of type Obsunit Finding the record by its ID Obsunit in
      * String format
      *
      * @param idObsunit
      * @return Obsunit
      */
     public Obsunit getObsunit(Integer idObsunit);
 
     /**
      * Gets a list of Objects Obsunit
      *
      * @return List
      */
     public List<Obsunit> getObsunitList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param obsunitFiltro Object to count total items
      */
     public int getTotalObsunit(Obsunit obsunitFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param obsunitFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Obsunit> getListObsunit(Obsunit obsunitFilter, int start, int pageSize, boolean paged);
 
     /**
      * Get number of rows for an effect id. For example you can retrieve row
      * number for Measurement Effect
      *
      * @param effectId
      * @return
      */
     public int getObservationsCount(final Integer effectId);
 
     /**
      * Gets a list of observations unit for a effect id
      *
      * @param effectId Effect Id to search
      * @return List of observations units or empty list
      */
     public List<Obsunit> getObsunitListByEffectid(final Integer effectId);
 
     //-----------------------------------Oindex---------------------------
     /**
      * Adds an Object Oindex to database
      *
      * @param oindex Objeto a agregar
      */
     public void addOindex(int experimentId, int projectId);
 
     /**
      * Updates a record of type Oindex in database
      *
      * @param oindex Objeto a actualizar
      */
     public void updateOindex(Oindex oindex);
 
     /**
      * Deletes an object Oindex from database
      *
      * @param oindex Objeto a eliminar
      */
     public void deleteOindex(Oindex oindex);
 
     /**
      * Gets an Object from database Oindex of the type Oindex
      *
      * @param oindex
      * @return Oindex
      */
 //    public Oindex getOindex(Oindex oindex);
     /**
      * Gets an Object of type Oindex Finding the record by its ID Oindex in
      * String format
      *
      * @param idOindex
      * @return Oindex
      */
 //    public Oindex getOindex(Integer idOindex);
     /**
      * Gets a list of Objects Oindex
      *
      * @return List
      */
     public List<Oindex> getOindexList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param oindexFiltro Object to count total items
      */
     public int getTotalOindex(Oindex oindexFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param oindexFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Oindex> getListOindex(Oindex oindexFilter, int start, int pageSize, boolean paged);
 
     /**
      * Return a list of Oindex by it represno
      *
      * @param represno respresno to search
      * @return List of Oindex or empty list if not records match
      */
     public List<Oindex> getOindexListByRepresno(final Integer represno);
     //-----------------------------------Persons---------------------------
 
     /**
      * Adds an Object Persons to database
      *
      * @param persons Objeto a agregar
      */
     public void addPersons(Persons persons);
 
     /**
      * Updates a record of type Persons in database
      *
      * @param persons Objeto a actualizar
      */
     public void updatePersons(Persons persons);
 
     /**
      * Deletes an object Persons from database
      *
      * @param persons Objeto a eliminar
      */
     public void deletePersons(Persons persons);
 
     /**
      * Gets an Object from database Persons of the type Persons
      *
      * @param persons
      * @return Persons
      */
     public Persons getPersons(Persons persons);
 
     /**
      * Gets an Object of type Persons Finding the record by its ID Persons in
      * String format
      *
      * @param idPersons
      * @return Persons
      */
     public Persons getPersons(Integer idPersons);
 
     /**
      * Gets a list of Objects Persons
      *
      * @return List
      */
     public List<Persons> getPersonsList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param personsFiltro Object to count total items
      */
     public int getTotalPersons(Persons personsFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param personsFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Persons> getListPersons(Persons personsFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Progntrs---------------------------
     /**
      * Adds an Object Progntrs to database
      *
      * @param progntrs Objeto a agregar
      */
     public void addProgntrs(Progntrs progntrs);
 
     /**
      * Updates a record of type Progntrs in database
      *
      * @param progntrs Objeto a actualizar
      */
     public void updateProgntrs(Progntrs progntrs);
 
     /**
      * Deletes an object Progntrs from database
      *
      * @param progntrs Objeto a eliminar
      */
     public void deleteProgntrs(Progntrs progntrs);
 
     /**
      * Gets an Object from database Progntrs of the type Progntrs
      *
      * @param progntrs
      * @return Progntrs
      */
 //    public Progntrs getProgntrs(Progntrs progntrs);
     /**
      * Gets an Object of type Progntrs Finding the record by its ID Progntrs in
      * String format
      *
      * @param idProgntrs
      * @return Progntrs
      */
 //    public Progntrs getProgntrs(Integer idProgntrs);
     /**
      * Gets a list of Objects Progntrs
      *
      * @return List
      */
     public List<Progntrs> getProgntrsList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param progntrsFiltro Object to count total items
      */
     public int getTotalProgntrs(Progntrs progntrsFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param progntrsFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Progntrs> getListProgntrs(Progntrs progntrsFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Represtn---------------------------
     /**
      * Adds an Object Represtn to database
      *
      * @param represtn Objeto a agregar
      */
     public void addReprestn(Represtn represtn);
 
     /**
      * Updates a record of type Represtn in database
      *
      * @param represtn Objeto a actualizar
      */
     public void updateReprestn(Represtn represtn);
 
     /**
      * Deletes an object Represtn from database
      *
      * @param represtn Objeto a eliminar
      */
     public void deleteReprestn(Represtn represtn);
 
     /**
      * Gets an Object from database Represtn of the type Represtn
      *
      * @param represtn
      * @return Represtn
      */
     public Represtn getReprestn(Represtn represtn);
 
     /**
      * Gets an Object of type Represtn Finding the record by its ID Represtn in
      * String format
      *
      * @param idReprestn
      * @return Represtn
      */
     public Represtn getReprestn(Integer idReprestn);
 
     /**
      * Gets a list of Objects Represtn
      *
      * @return List
      */
     public List<Represtn> getReprestnList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param represtnFiltro Object to count total items
      */
     public int getTotalReprestn(Represtn represtnFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param represtnFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Represtn> getListReprestn(Represtn represtnFilter, int start, int pageSize, boolean paged);
 
     /**
      * Gests a Representation object for a Study
      * @param studyId Study number
      * @param represName Representation name to find
      * @return
      */
     public Represtn getReprestnForStudyId(final Integer studyId, String represName);    
     //-----------------------------------Scale---------------------------
     /**
      * Adds an Object Scale to database
      *
      * @param scale Objeto a agregar
      */
     public void addScale(Scale scale);
 
     /**
      * Updates a record of type Scale in database
      *
      * @param scale Objeto a actualizar
      */
     public void updateScale(Scale scale);
 
     /**
      * Deletes an object Scale from database
      *
      * @param scale Objeto a eliminar
      */
     public void deleteScale(Scale scale);
 
     /**
      * Gets an Object from database Scale of the type Scale
      *
      * @param scale
      * @return Scale
      */
     public Scale getScale(Scale scale);
 
     /**
      * Gets an Object of type Scale Finding the record by its ID Scale in String
      * format
      *
      * @param idScale
      * @return Scale
      */
     public Scale getScale(Integer idScale);
 
     /**
      * Get scale diferent for groups by scname and sctype
      *
      * @return Integer
      */
     public Integer getScaleTotalGroup();
 
     /**
      * Gets a list of Objects Scale
      *
      * @return List
      */
     public List<Scale> getScaleList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param scaleFiltro Object to count total items
      */
     public int getTotalScale(Scale scaleFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param scaleFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Scale> getListScale(Scale scaleFilter, int start, int pageSize, boolean paged);
 
     /**
      * Gets a list of Objects Scale Groups
      *
      * @return List Scale
      */
     public List<Scale> getListScaleGroups();
 
     /**
      * Migrate scales to TMSScales Direct its correct a count(*)
      */
     public void migrateScaleToTmsscalesDirect();
 
     public List<Scale> getListScaleAll();
 
 //-----------------------------------Scales---------------------------
     /**
      * Adds an Object Scales to database
      *
      * @param scales Objeto a agregar
      */
     public void addScales(Scales scales);
     
     /**
      * Updates a record of type Scales in database
      *
      * @param scales Objeto a actualizar
      */
     public void updateScales(Scales scales);
 
     /**
      * Deletes an object Scales from database
      *
      * @param scales Objeto a eliminar
      */
     public void deleteScales(Scales scales);
 
     /**
      * Gets an Object from database Scales of the type Scales
      *
      * @param scales
      * @return Scales
      */
     public Scales getScales(Scales scales);
 
     /**
      * Gets an Object of type Scales Finding the record by its ID Scales in
      * String format
      *
      * @param idScale
      * @return Scales
      */
     public Scales getScales(Integer idScale);
 
     /**
      * Gets a list of Objects Scales
      *
      * @return List
      */
     public List<Scales> getScalesList();
 
     public List<Scales> getScalesListNew();
 
     public List<Scales> getListScalesByScaleNew(Scales scalesFilter, int start, int pageSize, boolean paged);
 
     /**
      * Gets the number of records matching with filter
      *
      * @param scalesFiltro Object to count total items
      */
     public int getTotalScales(Scales scalesFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param scalesFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Scales> getListScales(Scales scalesFilter, int start, int pageSize, boolean paged);
 
     /**
      * Return a scales
      *
      * @param scales
      * @return Scales
      */
     public Scales getScalesByScnameAndSctype(Scales scales);
 
     public Scales getScalesByScname(Scales scales);
 
     //-----------------------------------Scalecon---------------------------
     /**
      * Adds an Object Scalecon to database
      *
      * @param scalecon Objeto a agregar
      */
     public void addScalecon(Scalecon scalecon);
 
     /**
      * Updates a record of type Scalecon in database
      *
      * @param scalecon Objeto a actualizar
      */
     public void updateScalecon(Scalecon scalecon);
 
     /**
      * Deletes an object Scalecon from database
      *
      * @param scalecon Objeto a eliminar
      */
     public void deleteScalecon(Scalecon scalecon);
 
     /**
      * Gets an Object from database Scalecon of the type Scalecon
      *
      * @param scalecon
      * @return Scalecon
      */
 //    public Scalecon getScalecon(Scalecon scalecon);
     /**
      * Gets an Object of type Scalecon Finding the record by its ID Scalecon in
      * String format
      *
      * @param idScalecon
      * @return Scalecon
      */
 //    public Scalecon getScalecon(Integer idScalecon);
     /**
      * Gets a list of Objects Scalecon
      *
      * @return List
      */
     public List<Scalecon> getScaleconList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param scaleconFiltro Object to count total items
      */
     public int getTotalScalecon(Scalecon scaleconFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param scaleconFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Scalecon> getListScalecon(Scalecon scaleconFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Scaledis---------------------------
     /**
      * Adds an Object Scaledis to database
      *
      * @param scaledis Objeto a agregar
      */
     public void addScaledis(Scaledis scaledis);
 
     /**
      * Updates a record of type Scaledis in database
      *
      * @param scaledis Objeto a actualizar
      */
     public void updateScaledis(Scaledis scaledis);
 
     /**
      * Deletes an object Scaledis from database
      *
      * @param scaledis Objeto a eliminar
      */
     public void deleteScaledis(Scaledis scaledis);
 
     /**
      * Gets an Object from database Scaledis of the type Scaledis
      *
      * @param scaledis
      * @return Scaledis
      */
 //    public Scaledis getScaledis(Scaledis scaledis);
     /**
      * Gets an Object of type Scaledis Finding the record by its ID Scaledis in
      * String format
      *
      * @param idScaledis
      * @return Scaledis
      */
 //    public Scaledis getScaledis(Integer idScaledis);
     /**
      * Gets a list of Objects Scaledis
      *
      * @return List
      */
     public List<Scaledis> getScaledisList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param scaledisFiltro Object to count total items
      */
     public int getTotalScaledis(Scaledis scaledisFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param scaledisFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Scaledis> getListScaledis(Scaledis scaledisFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Scaletab---------------------------
     /**
      * Adds an Object Scaletab to database
      *
      * @param scaletab Objeto a agregar
      */
     public void addScaletab(Scaletab scaletab);
 
     /**
      * Updates a record of type Scaletab in database
      *
      * @param scaletab Objeto a actualizar
      */
     public void updateScaletab(Scaletab scaletab);
 
     /**
      * Deletes an object Scaletab from database
      *
      * @param scaletab Objeto a eliminar
      */
     public void deleteScaletab(Scaletab scaletab);
 
     /**
      * Gets an Object from database Scaletab of the type Scaletab
      *
      * @param scaletab
      * @return Scaletab
      */
     public Scaletab getScaletab(Scaletab scaletab);
 
     /**
      * Gets an Object of type Scaletab Finding the record by its ID Scaletab in
      * String format
      *
      * @param idScaletab
      * @return Scaletab
      */
     public Scaletab getScaletab(Integer idScaletab);
 
     /**
      * Gets a list of Objects Scaletab
      *
      * @return List
      */
     public List<Scaletab> getScaletabList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param scaletabFiltro Object to count total items
      */
     public int getTotalScaletab(Scaletab scaletabFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param scaletabFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Scaletab> getListScaletab(Scaletab scaletabFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Sndivs---------------------------
     /**
      * Adds an Object Sndivs to database
      *
      * @param sndivs Objeto a agregar
      */
     public void addSndivs(Sndivs sndivs);
 
     /**
      * Updates a record of type Sndivs in database
      *
      * @param sndivs Objeto a actualizar
      */
     public void updateSndivs(Sndivs sndivs);
 
     /**
      * Deletes an object Sndivs from database
      *
      * @param sndivs Objeto a eliminar
      */
     public void deleteSndivs(Sndivs sndivs);
 
     /**
      * Gets an Object from database Sndivs of the type Sndivs
      *
      * @param sndivs
      * @return Sndivs
      */
     public Sndivs getSndivs(Sndivs sndivs);
 
     /**
      * Gets an Object of type Sndivs Finding the record by its ID Sndivs in
      * String format
      *
      * @param idSndivs
      * @return Sndivs
      */
     public Sndivs getSndivs(Integer idSndivs);
 
     /**
      * Gets a list of Objects Sndivs
      *
      * @return List
      */
     public List<Sndivs> getSndivsList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param sndivsFiltro Object to count total items
      */
     public int getTotalSndivs(Sndivs sndivsFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param sndivsFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Sndivs> getListSndivs(Sndivs sndivsFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Steffect---------------------------
     /**
      * Adds an Object Steffect to database
      *
      * @param steffect Objeto a agregar
      */
     public void addSteffect(Steffect steffect);
 
     /**
      * Updates a record of type Steffect in database
      *
      * @param steffect Objeto a actualizar
      */
     public void updateSteffect(Steffect steffect);
 
     /**
      * Deletes an object Steffect from database
      *
      * @param steffect Objeto a eliminar
      */
     public void deleteSteffect(Steffect steffect);
 
     /**
      * Gets an Object from database Steffect of the type Steffect
      *
      * @param steffect
      * @return Steffect
      */
     public Steffect getSteffect(Steffect steffect);
 
     /**
      * Gets an Object of type Steffect Finding the record by its ID Steffect in
      * String format
      *
      * @param idSteffect
      * @return Steffect
      */
     public Steffect getSteffect(Integer idSteffect);
 
     /**
      * Gets a list of Objects Steffect
      *
      * @return List
      */
     public List<Steffect> getSteffectList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param steffectFiltro Object to count total items
      */
     public int getTotalSteffect(Steffect steffectFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param steffectFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Steffect> getListSteffect(Steffect steffectFilter, int start, int pageSize, boolean paged);
 
     public List<Steffect> getSteffectByStudyid(Integer studyid);
 
     public List<Integer> getEffectidsByStudyid(Integer studyid);
 
     //-----------------------------------Study---------------------------
     /**
      * Adds an Object Study to database
      *
      * @param study Objeto a agregar
      */
     public void addStudy(Study study);
 
     /**
      * Updates a record of type Study in database
      *
      * @param study Objeto a actualizar
      */
     public void updateStudy(Study study);
 
     /**
      * Deletes an object Study from database
      *
      * @param study Objeto a eliminar
      */
     public void deleteStudy(Study study);
 
     /**
      * Gets an Object from database Study of the type Study
      *
      * @param study
      * @return Study
      */
     public Study getStudy(Study study);
 
     /**
      * Gets an Object of type Study Finding the record by its ID Study in String
      * format
      *
      * @param idStudy
      * @return Study
      */
     public Study getStudy(Integer idStudy);
 
     /**
      * Gets a list of Objects Study
      *
      * @return List
      */
     public List<Study> getStudyList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param studyFiltro Object to count total items
      */
     public int getTotalStudy(Study studyFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param studyFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Study> getListStudy(Study studyFilter, int start, int pageSize, boolean paged);
     
     public ResultSet getTrialRandomization(
             Integer studyId,
             Integer trialFactorId,
             List<String> factoresPrincipales,
             List<String> factoresSalida,
             String nombreTrial);
     
     public ResultSet getTrialRandomizationFast(
             Integer studyId,
             Integer trialFactorId,
             List<String> factoresPrincipales,
             List<String> factoresSalida,
             String nombreTrial);
     
     public List<Measurement> getTrialRandomizationVeryFast(
             Integer studyId,
             Integer trialFactorId,
             List<String> factoresPrincipales,
             List<String> factoresSalida,
             String nombreTrial);
     
     public StudySearch getListGermplasmAndPlotByStudyidAndTrial(
             StudySearch studySearch);
     
     public StudySearch getListGermplasmAndPlotByStudyidAndTrial(
             StudySearch studySearch,
             List<String> factorsKey,
             List<String> factorsReturn
             );
 
     /**
      * Return a studys
      *
      * @return List<Study>
      */
     public List<Study> getStudysOnlyTrial();
 
 //-----------------------------------Tmethod---------------------------
     /**
      * Adds an Object Tmethod to database
      *
      * @param tmethod Objeto a agregar
      */
     public void addTmethod(Tmethod tmethod);
 
     /**
      * Updates a record of type Tmethod in database
      *
      * @param tmethod Objeto a actualizar
      */
     public void updateTmethod(Tmethod tmethod);
 
     /**
      * Deletes an object Tmethod from database
      *
      * @param tmethod Objeto a eliminar
      */
     public void deleteTmethod(Tmethod tmethod);
 
     /**
      * Gets an Object from database Tmethod of the type Tmethod
      *
      * @param tmethod
      * @return Tmethod
      */
     public Tmethod getTmethod(Tmethod tmethod);
 
     /**
      * Gets an Object of type Tmethod Finding the record by its ID Tmethod in
      * String format
      *
      * @param idTmethod
      * @return Tmethod
      */
     public Tmethod getTmethod(Integer idTmethod);
 
     /**
      * Gets a list of Objects Tmethod
      *
      * @return List
      */
     public List<Tmethod> getTmethodList();
 
 
     /**
      * Method for retrieving tmethods without using the cvterm_relationship table
      *
      * @return
      */
     public List<TmsMethod> getTmsMethodListNew();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param tmethodFiltro Object to count total items
      */
     public int getTotalTmethod(Tmethod tmethodFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param tmethodFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Tmethod> getListTmethod(Tmethod tmethodFilter, int start, int pageSize, boolean paged);
 
     /**
      * Returns a list of Tmethod objects given a filter. Items are retrieved without making use of the cvterm_relationship table.
      *
      * @param filter
      * @param start
      * @param pageSize
      * @param paged
      * @return
      */
     public List<TmsMethod> getListTmsMethodNew(TmsMethod filter, int start, int pageSize, boolean paged);
 
 //-----------------------------------Tmethod---------------------------
     /**
      * Adds an Object TmsMethod to database
      *
      * @param tmsMethod Objeto a agregar
      */
     public void addTmsMethod(TmsMethod tmsMethod);
     
     /**
      * Updates a record of type TmsMethod in database
      *
      * @param tmsMethod Objeto a actualizar
      */
     public void updateTmsMethod(TmsMethod tmsMethod);
 
     /**
      * Deletes an object TmsMethod from database
      *
      * @param tmsMethod Objeto a eliminar
      */
     public void deleteTmsMethod(TmsMethod tmsMethod);
 
     /**
      * Gets an Object from database TmsMethod of the type TmsMethod
      *
      * @param tmsMethod
      * @return TmsMethod
      */
     public TmsMethod getTmsMethod(TmsMethod tmsMethod);
 
     /**
      * Gets an Object of type TmsMethod Finding the record by its ID TmsMethod
      * in String format
      *
      * @param idTmethod
      * @return TmsMethod
      */
     public TmsMethod getTmsMethod(Integer idTmethod);
 
     /**
      * Gets a list of Objects TmsMethod
      *
      * @return List
      */
     public List<TmsMethod> getTmsMethodList();
 
 
 
     /**
      * Gets the number of records matching with filter
      *
      * @param tmsMethodFiltro Object to count total items
      */
     public int getTotalTmsMethod(TmsMethod tmsMethodFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param tmethodFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<TmsMethod> getListTmsMethod(TmsMethod tmsMethodFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------TmsScaleCon---------------------------
     /**
      * Adds an Object TmsScaleCon to database
      *
      * @param tmsScaleCon Objeto a agregar
      */
     public void addTmsScaleCon(TmsScaleCon tmsScaleCon);
 
     /**
      * Adds or updates an Object TmsScaleCon to database
      *
      * @param tmsScaleCon Objeto a agregar
      */
     public void addOrUpdateTmsScaleCon(TmsScaleCon tmsScaleCon);
 
     /**
      * Updates a record of type TmsScaleCon in database
      *
      * @param tmsScaleCon Objeto a actualizar
      */
     public void updateTmsScaleCon(TmsScaleCon tmsScaleCon);
 
     /**
      * Deletes an object TmsScaleCon from database
      *
      * @param tmsScaleCon Objeto a eliminar
      */
     public void deleteTmsScaleCon(TmsScaleCon tmsScaleCon);
 
     /**
      * Gets an Object from database TmsScaleCon of the type TmsScaleCon
      *
      * @param tmsScaleCon
      * @return TmsScaleCon
      */
     public TmsScaleCon getTmsScaleCon(TmsScaleCon tmsScaleCon);
 
     /**
      * Gets an Object of type TmsScaleCon Finding the record by its ID
      * TmsScaleCon in String format
      *
      * @param idTmethod
      * @return TmsScaleCon
      */
     public TmsScaleCon getTmsScaleCon(Integer idTmethod);
 
     /**
      * Gets a list of Objects TmsScaleCon
      *
      * @return List
      */
     public List<TmsScaleCon> getTmsScaleConList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param tmsScaleConFiltro Object to count total items
      */
     public int getTotalTmsScaleCon(TmsScaleCon tmsScaleConFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param tmethodFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<TmsScaleCon> getListTmsScaleCon(TmsScaleCon tmsScaleConFilter, int start, int pageSize, boolean paged);
 
     /**
      * Gets a list of TmsScaleCon by it measuredin id
      *
      * @param measuredindid measuredin to search
      * @return list of TmsScaleCon or empty list
      */
     public List<TmsScaleCon> getTmsScaleConByMeasuredinId(final Integer measuredindid);
 
     /**
      * Gets a ScaleCon by Measured In ID
      *
      * @param Measured In ID
      * @return TmsScaleCon Object if found, if not it returns NULL
      */
     public TmsScaleCon getScaleConByMeasuredinId(final Integer measuredinId);
 
 //-----------------------------------TmsScaleDis---------------------------
     /**
      * Adds an Object TmsScaleDis to database
      *
      * @param tmsScaleDis Objeto a agregar
      */
     public void addTmsScaleDis(TmsScaleDis tmsScaleDis);
 
     /**
      * Adds or updates an Object TmsScaleCon to database
      *
      * @param tmsScaleCon Objeto a agregar
      */
     public void addOrUpdateTmsScaleDis(TmsScaleDis tmsScaleDis);
 
     /**
      * Updates a record of type TmsScaleDis in database
      *
      * @param tmsScaleDis Objeto a actualizar
      */
     public void updateTmsScaleDis(TmsScaleDis tmsScaleDis);
 
     /**
      * Deletes an object TmsScaleDis from database
      *
      * @param tmsScaleDis Objeto a eliminar
      */
     public void deleteTmsScaleDis(TmsScaleDis tmsScaleDis);
 
     /**
      * Gets an Object from database TmsScaleDis of the type TmsScaleDis
      *
      * @param tmsScaleDis
      * @return TmsScaleDis
      */
     public TmsScaleDis getTmsScaleDis(TmsScaleDis tmsScaleDis);
 
     /**
      * Gets an Object of type TmsScaleDis Finding the record by its ID
      * TmsScaleDis in String format
      *
      * @param idTmethod
      * @return TmsScaleDis
      */
     public TmsScaleDis getTmsScaleDis(Integer idTmethod);
 
     /**
      * Gets a list of Objects TmsScaleDis
      *
      * @return List
      */
     public List<TmsScaleDis> getTmsScaleDisList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param tmsScaleDisFiltro Object to count total items
      */
     public int getTotalTmsScaleDis(TmsScaleDis tmsScaleDisFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param tmethodFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<TmsScaleDis> getListTmsScaleDis(TmsScaleDis tmsScaleDisFilter, int start, int pageSize, boolean paged);
 
     /**
      * Gets a list of TmsScaleDis by it measuredin id
      *
      * @param measuredindid measuredin to search
      * @return list of TmsScaleDis or empty list
      */
     public List<TmsScaleDis> getTmsScaleDisByMeasuredinId(final Integer measuredindid);
 
     /**
      * Gets a TmsScaleDis by Measured In ID
      *
      * @param Measured In ID
      * @return TmsScaleDis Object if found, if not it returns NULL
      */
     public TmsScaleDis getScaleDisByMeasuredinId(final Integer measuredinId);
 
     //-----------------------------------Trait---------------------------
     /**
      * Adds an Object Trait to database
      *
      * @param trait Objeto a agregar
      */
     public void addTrait(Trait trait);
     
     
 
     /**
      * Updates a record of type Trait in database
      *
      * @param trait Objeto a actualizar
      */
     public void updateTrait(Trait trait);
 
     /**
      * Deletes an object Trait from database
      *
      * @param trait Objeto a eliminar
      */
     public void deleteTrait(Trait trait);
 
     /**
      * Gets an Object from database Trait of the type Trait
      *
      * @param trait
      * @return Trait
      */
     public Trait getTrait(Trait trait);
 
     /**
      * Gets an Object of type Trait Finding the record by its ID Trait in String
      * format
      *
      * @param idTrait
      * @return Trait
      */
     public Trait getTrait(Integer idTrait);
 
     /**
      * Gets a list of Objects Trait
      *
      * @return List
      */
     public List<Trait> getTraitList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param traitFiltro Object to count total items
      */
     public int getTotalTrait(Trait traitFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param traitFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Trait> getListTrait(Trait traitFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Traits---------------------------
     /**
      * Adds an Object Traits to database
      *
      * @param traits Objeto a agregar
      */
     public void addTraits(Traits traits);
   
     /**
      * Gets a list of all different trait groups
      *
      * @return
      */
     public List<String> getTraitGroups();
 
     /**
      * Updates a record of type Traits in database
      *
      * @param traits Objeto a actualizar
      */
     public void updateTraits(Traits traits);
 
     /**
      * Deletes an object Traits from database
      *
      * @param traits Objeto a eliminar
      */
     public void deleteTraits(Traits traits);
 
     /**
      * Gets an Object from database Traits of the type Traits
      *
      * @param traits
      * @return Traits
      */
     public Traits getTraits(Traits traits);
 
     /**
      * Gets an Object of type Traits Finding the record by its ID Traits in
      * String format
      *
      * @param idTraits
      * @return Traits
      */
     public Traits getTraits(Integer idTraits);
 
     /**
      * Gets an Object of type Traits Finding the record by its ID Traits in
      * String format
      *
      * @param idTraits
      * @return Traits
      */
     public Traits getTraitsByTraitid(Integer idTraits);
 
     /**
      * Gets a list of Objects Traits
      *
      * @return List
      */
     public List<Traits> getTraitsList();
 
     public List<Traits> getTraitsListNew();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param traitFiltro Object to count total items
      */
     public int getTotalTraits(Traits traitsFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param traitsFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Traits> getListTraits(Traits traitsFilter, int start, int pageSize, boolean paged);
 
     public List<Traits> getListTraitsNew(Traits filter, int start, int pageSize, boolean paged);
 
     public Traits getTraitsByTrname(Traits traits);
 
     //-----------------------------------Udflds---------------------------
     /**
      * Adds an Object Udflds to database
      *
      * @param udflds Objeto a agregar
      */
     public void addUdflds(Udflds udflds);
 
     /**
      * Updates a record of type Udflds in database
      *
      * @param udflds Objeto a actualizar
      */
     public void updateUdflds(Udflds udflds);
 
     /**
      * Deletes an object Udflds from database
      *
      * @param udflds Objeto a eliminar
      */
     public void deleteUdflds(Udflds udflds);
 
     /**
      * Gets an Object from database Udflds of the type Udflds
      *
      * @param udflds
      * @return Udflds
      */
     public Udflds getUdflds(Udflds udflds);
 
     /**
      * Gets an Object of type Udflds Finding the record by its ID Udflds in
      * String format
      *
      * @param idUdflds
      * @return Udflds
      */
     public Udflds getUdflds(Integer idUdflds);
 
     /**
      * Gets a list of Objects Udflds
      *
      * @return List
      */
     public List<Udflds> getUdfldsList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param udfldsFiltro Object to count total items
      */
     public int getTotalUdflds(Udflds udfldsFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param udfldsFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Udflds> getListUdflds(Udflds udfldsFilter, int start, int pageSize, boolean paged);
 
     //-----------------------------------Users---------------------------
     /**
      * Adds an Object Users to database
      *
      * @param users Objeto a agregar
      */
     public void addUsers(Users users);
 
     /**
      * Updates a record of type Users in database
      *
      * @param users Objeto a actualizar
      */
     public void updateUsers(Users users);
 
     /**
      * Deletes an object Users from database
      *
      * @param users Objeto a eliminar
      */
     public void deleteUsers(Users users);
 
     /**
      * Gets an Object from database Users of the type Users
      *
      * @param users
      * @return Users
      */
     public Users getUsers(Users users);
 
     /**
      * Gets an Object of type Users Finding the record by its ID Users in String
      * format
      *
      * @param idUsers
      * @return Users
      */
     public Users getUsers(Integer idUsers);
 
     /**
      * Gets a list of Objects Users
      *
      * @return List
      */
     public List<Users> getUsersList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param usersFiltro Object to count total items
      */
     public int getTotalUsers(Users usersFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param usersFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Users> getListUsers(Users usersFilter, int start, int pageSize, boolean paged);
 
     /**
      * get ID for logged user according to following parameters USTATUS = 1 UACC
      * = 100 LOCAL ICIS ADMINISTRATOR UTYPE = 422 LOCAL DATABASE ADMINISTRATOR
      *
      * @return
      */
     public Integer getLoggedUserId();
 
     //-----------------------------------Variate---------------------------
     /**
      * Adds an Object Variate to database
      *
      * @param variate Objeto a agregar
      */
     public void addVariate(Variate variate);
 
     /**
      * Updates a record of type Variate in database
      *
      * @param variate Objeto a actualizar
      */
     public void updateVariate(Variate variate);
 
     /**
      * Deletes an object Variate from database
      *
      * @param variate Objeto a eliminar
      */
     public void deleteVariate(Variate variate);
 
     /**
      * Gets an Object from database Variate of the type Variate
      *
      * @param variate
      * @return Variate
      */
     public Variate getVariate(Variate variate);
 
     /**
      * Gets an Object of type Variate Finding the record by its ID Variate in
      * String format
      *
      * @param idVariate
      * @return Variate
      */
     public Variate getVariate(Integer idVariate);
 
     /**
      * Gets a list of Objects Variate
      *
      * @return List
      */
     public List<Variate> getVariateList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param variateFiltro Object to count total items
      */
     public int getTotalVariate(Variate variateFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param variateFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Variate> getListVariate(Variate variateFilter, int start, int pageSize, boolean paged);
 
     /**
      * Returns different combinations of TRAIT, SCALE and METHOD
      *
      * @return List<Variate>
      */
     public List<Variate> getVariateConvinacionesTraitScaleMethod();
 
     //-----------------------------------Veffect---------------------------
     /**
      * Adds an Object Veffect to database
      *
      * @param veffect Objeto a agregar
      */
     public void addVeffect(Veffect veffect);
 
     /**
      * Updates a record of type Veffect in database
      *
      * @param veffect Objeto a actualizar
      */
     public void updateVeffect(Veffect veffect);
 
     /**
      * Deletes an object Veffect from database
      *
      * @param veffect Objeto a eliminar
      */
     public void deleteVeffect(Veffect veffect);
 
     /**
      * Gets an Object from database Veffect of the type Veffect
      *
      * @param veffect
      * @return Veffect
      */
 //    public Veffect getVeffect(Veffect veffect);
     /**
      * Gets an Object of type Veffect Finding the record by its ID Veffect in
      * String format
      *
      * @param idVeffect
      * @return Veffect
      */
 //    public Veffect getVeffect(Integer idVeffect);
     /**
      * Gets a list of Objects Veffect
      *
      * @return List
      */
     public List<Veffect> getVeffectList();
 
     /**
      * Gets the number of records matching with filter
      *
      * @param veffectFiltro Object to count total items
      */
     public int getTotalVeffect(Veffect veffectFilter);
 
     /**
      * Gets a list of Objects for pagination
      *
      * @param veffectFiltro	The filter object
      * @param inicio initial record
      * @param tamanioPagina page size
      * @return List
      */
     public List<Veffect> getListVeffect(Veffect veffectFilter, int start, int pageSize, boolean paged);
 
     /**
      * Checks if Tratis, Scales and Measuredin tables already exists in database
      *
      * @return
      * <code>true</code> if exists,
      * <code>false</code> if does not exist.
      */
     public boolean existsTratisTable();
 
     /**
      * Create TraTratis, Scales and Measuredin
      */
     public void createTraitsTables();
 
     /**
      * Return a list of Variates where variate ID are stored in VEFFECT table
      * according to represno ID
      *
      * @param represenoId represno ID for resprestn number
      * @return list of Variates
      */
     public List<Variate> getVarieteFromVeffects(final Integer represenoId);
 
     public boolean isLocal();
 
     public boolean isCentral();
 
 //-----------------------------------ImsLabelOtherinfo---------------------------
     public void addImsLabelOtherinfo(ImsLabelOtherinfo imsLabelOtherinfo);
 
     public void updateImsLabelOtherinfo(ImsLabelOtherinfo imsLabelOtherinfo);
 
     public void deleteImsLabelOtherinfo(ImsLabelOtherinfo imsLabelOtherinfo);
 
     public List<ImsLabelOtherinfo> getImsLabelOtherinfoList();
 
     public int getTotalImsLabelOtherinfo(ImsLabelOtherinfo imsLabelOtherinfo);
 
     public List<ImsLabelOtherinfo> getListImsLabelOtherinfo(ImsLabelOtherinfo filter, int start, int pageSize, boolean paged);
 
 //-----------------------------------ImsLabelinfo---------------------------
     public void addImsLabelinfo(ImsLabelinfo imsLabelinfo);
 
     public void updateImsLabelinfo(ImsLabelinfo imsLabelinfo);
 
     public void deleteImsLabelinfo(ImsLabelinfo imsLabelinfo);
 
     public List<ImsLabelinfo> getImsLabelinfoList();
 
     public int getTotalImsLabelinfo(ImsLabelinfo imsLabelinfo);
 
     public List<ImsLabelinfo> getListImsLabelinfo(ImsLabelinfo filter, int start, int pageSize, boolean paged);
 //-----------------------------------ImsLot---------------------------
 
     public void addImsLot(ImsLot imsLot);
 
     public void updateImsLot(ImsLot imsLot);
 
     public void deleteImsLot(ImsLot imsLot);
 
     public List<ImsLot> getImsLotList();
 
     public int getTotalImsLot(ImsLot imsLot);
 
     public List<ImsLot> getListImsLot(ImsLot filter, int start, int pageSize, boolean paged);
 
 //-----------------------------------ImsTransaction---------------------------
     public void addImsTransaction(ImsTransaction imsTransaction);
 
     public void updateImsTransaction(ImsTransaction imsTransaction);
 
     public void deleteImsTransaction(ImsTransaction imsTransaction);
 
     public List<ImsTransaction> getImsTransactionList();
 
     public int getTotalImsTransaction(ImsTransaction imsTransaction);
 
     public List<ImsTransaction> getListImsTransaction(ImsTransaction filter, int start, int pageSize, boolean paged);
 
     /**
      * Get information data from a List
      *
      * @param listId Id for LIST
      * @return
      */
     public List<InventoryData> getInventoryDataFromList(final Integer listId);
     
     /**
      * Gets a different list of Location ID for that list
      * @param listId
      * @return 
      */
     public List<Integer> locationsForInventoryList(final Integer listId);
     
     /**
      * Gets a different list of Scales ID for that list
      * @param listId
      * @return 
      */
     public List<Integer> scalesForInventoryList(final Integer listId);    
     
     //-----------------------------------ContinuousConversion---------------------------
     public void addContinuousConversion(ContinuousConversion continuousConversion);
     
     public void updateContinuousConversion(ContinuousConversion continuousConversion);
     
     public void deleteContinuousConversion(ContinuousConversion continuousConversion);
     
     public ContinuousConversion getContinuousConversion(ContinuousConversion continuousConversion);
     
     public ContinuousConversion getContinuousConversion(Integer idAtributs);
     
     public List<ContinuousConversion> getContinuousConversionList();
     
     public int getTotalContinuousConversion(ContinuousConversion continuousConversion);
     
     public List<ContinuousConversion> getListContinuousConversion(ContinuousConversion filter, int start, int pageSize, boolean paged);
     
     public boolean existsTableContinuousConversion();
     
     public void createTableContinuousConversion();
     
     //-----------------------------------ContinuousFunction---------------------------
     public void addContinuousFunction(ContinuousFunction continuousFunction);
 
     public void updateContinuousFunction(ContinuousFunction continuousFunction);
 
     public void deleteContinuousFunction(ContinuousFunction continuousFunction);
 
     public ContinuousFunction getContinuousFunction(ContinuousFunction continuousFunction);
 
     public ContinuousFunction getContinuousFunction(Integer idAtributs);
 
     public List<ContinuousFunction> getContinuousFunctionList();
 
     public int getTotalContinuousFunction(ContinuousFunction continuousFunction);
 
     public List<ContinuousFunction> getListContinuousFunction(ContinuousFunction filter, int start, int pageSize, boolean paged);
     
     public boolean existsTableContinuousFunction();
     
     public void createTableContinuousFunction();
     
     //-----------------------------------DiscreteConversion---------------------------
     public void addDiscreteConversion(DiscreteConversion discreteConversion);
 
     public void updateDiscreteConversion(DiscreteConversion discreteConversion);
 
     public void deleteDiscreteConversion(DiscreteConversion discreteConversion);
 
     public DiscreteConversion getDiscreteConversion(DiscreteConversion discreteConversion);
 
     public DiscreteConversion getDiscreteConversion(Integer idAtributs);
 
     public List<DiscreteConversion> getDiscreteConversionList();
 
     public int getTotalDiscreteConversion(DiscreteConversion discreteConversion);
 
     public List<DiscreteConversion> getListDiscreteConversion(DiscreteConversion filter, int start, int pageSize, boolean paged);
     
     public boolean existsTableDiscreteConversion();
     
     public void createTableDiscreteConversion();
     
     //-----------------------------------Transformations---------------------------
     public void addTransformations(Transformations transformations);
     
     public void updateTransformations(Transformations transformations);
     
     public void deleteTransformations(Transformations transformations);
     
     public Transformations getTransformations(Transformations transformations);
     
     public Transformations getTransformations(Integer transid);
     
     public List<Transformations> getTransformationsList();
     
     public int getTotalTransformations(Transformations transformations);
     
     public List<Transformations> getListTransformations(Transformations filter, int start, int pageSize, boolean paged);    
     
     public boolean existsTableTransformations();    
     
     public void createTableTransformations();
     
     //-----------------------------------TmsConsistencyChecks---------------------------
     public void addTmsConsistencyChecks(TmsConsistencyChecks tmsConsistencyChecks);
 
     public void updateTmsConsistencyChecks(TmsConsistencyChecks tmsConsistencyChecks);
 
     public void deleteTmsConsistencyChecks(TmsConsistencyChecks tmsConsistencyChecks);
 
     public TmsConsistencyChecks getTmsConsistencyChecks(TmsConsistencyChecks tmsConsistencyChecks);
 
     public TmsConsistencyChecks getTmsConsistencyChecks(Integer transid);
 
     public List<TmsConsistencyChecks> getTmsConsistencyChecksList();
 
     public int getTotalTmsConsistencyChecks(TmsConsistencyChecks tmsConsistencyChecks);
 
     public List<TmsConsistencyChecks> getListTmsConsistencyChecks(TmsConsistencyChecks filter, int start, int pageSize, boolean paged);
     
     public boolean existsTableTmsConsistencyChecks();
     
     public void createTableTmsConsistencyChecks();
     
     
 
     /**
      * @return the accessUrlGms
      */
     public String getAccessUrlGms();
 
     /**
      * @return the accessUrlDms
      */
     public String getAccessUrlDms();
     
     /**
      * Gets a list of Udffields accoding to a table and a field related
      * @param tableName Table name
      * @param fieldName Field name 
      * @return List of Udflds objects
      */
     public List <Udflds> getUdfldsList(final String tableName, final String fieldName);
 
    public Integer addNdGeolocation(String description);
     public Integer addStock(String uniquename,String dbxref_id,String name,String value);
     public Integer addNdExperiment(Integer ndGeolocationId, Integer typeId);
     public Integer addNdExperimentStock(Integer ndExperimentId, Integer stockId);
     
     public void copyCvTermFromCentral(int cvTermId);
     
     public Integer getStoredInId(int traitid, int scaleid, int methodid);
     public List<Factor> getFactorsByStudyId(int studyId);
     public List<Variate> getStudyConstants(int studyId);
 }
