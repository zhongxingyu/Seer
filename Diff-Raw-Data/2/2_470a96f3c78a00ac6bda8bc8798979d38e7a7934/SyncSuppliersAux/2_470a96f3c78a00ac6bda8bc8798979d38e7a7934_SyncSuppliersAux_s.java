 package pt.ist.expenditureTrackingSystem.domain;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import myorg.domain.util.Address;
 import myorg.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.persistenceTier.ExternalDbOperation;
 import pt.ist.expenditureTrackingSystem.persistenceTier.ExternalDbQuery;
 
public class SyncSuppliersAux extends SyncSuppliers_Base {
 
     public static class GiafSupplier {
 	public String codEnt;
 	public String numFis;
 	public String nom_ent;
 	public String nom_ent_abv;
 	public boolean canceled = false;
 
 	public String ruaEnt = " ";
 	public String locEnt = " ";
 	public String codPos = " ";
 	public String codPai = " ";
 	public String telEnt = " ";
 	public String faxEnt = " ";
 	public String email = " ";
 
 	GiafSupplier(final ResultSet resultSet) throws SQLException {
 	    codEnt = get(resultSet, 1);
 	    numFis = get(resultSet, 2);
 	    nom_ent = get(resultSet, 3);
 	    nom_ent_abv = get(resultSet, 4);
 	}
 
 	public void setContactInformation(final ResultSet resultSet) throws SQLException {
 	    ruaEnt = get(resultSet, 2);
 	    locEnt = get(resultSet, 3);
 	    codPos = get(resultSet, 4);
 	    codPai = get(resultSet, 5);
 	    telEnt = get(resultSet, 6);
 	    faxEnt = get(resultSet, 7);
 	    email = get(resultSet, 8);
 	}
 
 	private String get(final ResultSet resultSet, final int i) throws SQLException {
 	    final String string = resultSet.getString(i);
 	    return string == null || string.length() == 0 ? " " : string.replace('\n', ' ').replace('\t', ' ');
 	}
     }
 
     public static class SupplierMap {
 
 	public Map<String, GiafSupplier> giafCodEntMap = new HashMap<String, GiafSupplier>();
 	public Map<String, GiafSupplier> giafFiscalIdMap = new HashMap<String, GiafSupplier>();
 
 	public void index(final GiafSupplier giafSupplier) {
 	    giafCodEntMap.put(giafSupplier.codEnt, giafSupplier);
 	    giafFiscalIdMap.put(giafSupplier.numFis, giafSupplier);
 	}
 
 	public GiafSupplier getGiafSupplierByFiscalId(final String fiscalIdentificationCode) {
 	    return giafFiscalIdMap.get(fiscalIdentificationCode);
 	}
 
 	public GiafSupplier getGiafSupplierByGiafKey(final String giafKey) {
 	    return giafCodEntMap.get(giafKey);
 	}
 
 	public Collection<GiafSupplier> getGiafSuppliers() {
 	    return giafCodEntMap.values();
 	}
     }
 
     private static class SupplierQuery extends ExternalDbQuery {
 
 	private final SupplierMap supplierMap;
 
 	SupplierQuery(final SupplierMap supplierMap) {
 	    this.supplierMap = supplierMap;
 	}
 
 	@Override
 	protected String getQueryString() {
 	    return "SELECT GIDFORN.forn_cod_ent, GIDENTGER.num_fis, GIDENTGER.nom_ent, GIDENTGER.nom_ent_abv"
 		    + " FROM GIDFORN, GIDENTGER where GIDFORN.forn_cod_ent = GIDENTGER.cod_ent";
 	}
 
 	@Override
 	protected void processResultSet(final ResultSet resultSet) throws SQLException {
 	    while (resultSet.next()) {
 		final GiafSupplier giafSupplier = new GiafSupplier(resultSet);
 		supplierMap.index(giafSupplier);
 	    }
 	}
 
     }
 
     private static class SupplierContactQuery extends ExternalDbQuery {
 
 	private final SupplierMap supplierMap;
 
 	SupplierContactQuery(final SupplierMap supplierMap) {
 	    this.supplierMap = supplierMap;
 	}
 
 	@Override
 	protected String getQueryString() {
 	    return "SELECT cod_ent, rua_ent, loc_ent, cod_pos, cod_pai, tel_ent, fax_ent, email FROM GIDMORADA";
 	}
 
 	@Override
 	protected void processResultSet(final ResultSet resultSet) throws SQLException {
 	    while (resultSet.next()) {
 		final String codEnt = resultSet.getString(1);
 		final GiafSupplier giafSupplier = findRemoteSupplier(codEnt);
 		if (giafSupplier != null) {
 		    giafSupplier.setContactInformation(resultSet);
 		}
 	    }
 	}
 
 	private GiafSupplier findRemoteSupplier(final String codEnt) {
 	    for (final GiafSupplier giafSupplier : supplierMap.getGiafSuppliers()) {
 		if (giafSupplier.codEnt.equals(codEnt)) {
 		    return giafSupplier;
 		}
 	    }
 	    return null;
 	}
     }
 
     private static class CanceledSupplierQuery extends ExternalDbQuery {
 
 	private final SupplierMap supplierMap;
 
 	CanceledSupplierQuery(final SupplierMap supplierMap) {
 	    this.supplierMap = supplierMap;
 	}
 
 	@Override
 	protected String getQueryString() {
 	    return "select * from (" + "SELECT ENTC_COD_ENT, max(ENTC_DAT_CAN) as cancelamento, max(ENTC_DAT_ACT) as activacao "
 		    + "FROM GIDENTCAN group by ENTC_COD_ENT) " + "where activacao is null or activacao < cancelamento";
 	}
 
 	@Override
 	protected void processResultSet(final ResultSet resultSet) throws SQLException {
 	    while (resultSet.next()) {
 		final String codEnt = resultSet.getString(1);
 		final GiafSupplier giafSupplier = findRemoteSupplier(codEnt);
 		if (giafSupplier != null) {
 		    giafSupplier.canceled = true;
 		}
 	    }
 	}
 
 	private GiafSupplier findRemoteSupplier(final String codEnt) {
 	    for (final GiafSupplier giafSupplier : supplierMap.getGiafSuppliers()) {
 		if (giafSupplier.codEnt.equals(codEnt)) {
 		    return giafSupplier;
 		}
 	    }
 	    return null;
 	}
     }
 
     public static class SupplierReader extends ExternalDbOperation {
 
 	private final SupplierMap supplierMap;
 
 	public SupplierReader(final SupplierMap supplierMap) {
 	    this.supplierMap = supplierMap;
 	}
 
 	@Override
 	protected String getDbPropertyPrefix() {
 	    return "db.giaf";
 	}
 
 	@Override
 	protected void doOperation() throws SQLException {
 	    final SupplierQuery supplierQuery = new SupplierQuery(supplierMap);
 	    executeQuery(supplierQuery);
 
 	    final SupplierContactQuery supplierContactQuery = new SupplierContactQuery(supplierMap);
 	    executeQuery(supplierContactQuery);
 
 	    final CanceledSupplierQuery canceledSupplierQuery = new CanceledSupplierQuery(supplierMap);
 	    executeQuery(canceledSupplierQuery);
 	}
 
     }
 
     public static void syncData() throws IOException, SQLException {
 	final SupplierMap supplierMap = new SupplierMap();
 	final SupplierReader supplierReader = new SupplierReader(supplierMap);
 	supplierReader.execute();
 
 	System.out.println("Read " + supplierMap.giafCodEntMap.size() + " suppliers from giaf.");
 
 	int matched = 0;
 	int created = 0;
 	int discarded = 0;
 	final Set<Supplier> suppliersFromGiaf = new HashSet<Supplier>();
 	for (final GiafSupplier giafSupplier : supplierMap.getGiafSuppliers()) {
 	    if (giafSupplier.canceled || shouldDiscard(giafSupplier)) {
 		discarded++;
 	    }
 	    Supplier supplier = findSupplierByGiafKey(giafSupplier.codEnt);
 	    if (supplier == null && !giafSupplier.canceled && !shouldDiscard(giafSupplier)) {
 		final String country = getCountry(giafSupplier);
 		final Address address = new Address(giafSupplier.ruaEnt, null, giafSupplier.codPos, giafSupplier.locEnt, country);
 		supplier = new Supplier(giafSupplier.nom_ent, giafSupplier.nom_ent_abv, giafSupplier.numFis, address,
 			giafSupplier.telEnt, giafSupplier.faxEnt, giafSupplier.email, null);
 		supplier.setGiafKey(giafSupplier.codEnt);
 		created++;
 	    } else if (supplier != null) {
 		matched++;
 		updateSupplierInformation(supplier, giafSupplier);
 	    }
 	    suppliersFromGiaf.add(supplier);
 	}
 
 	closeLocalSuppliers(suppliersFromGiaf);
 
 	System.out.println("Matched: " + matched + " suppliers.");
 	System.out.println("Created: " + created + " suppliers.");
 	System.out.println("Discarded: " + discarded + " suppliers.");
     }
 
     private static void closeLocalSuppliers(final Set<Supplier> suppliersFromGiaf) {
 	final ExpenditureTrackingSystem expenditureTrackingSystem = ExpenditureTrackingSystem.getInstance();
 	for (final Supplier supplier : expenditureTrackingSystem.getSuppliersSet()) {
 	    if (!suppliersFromGiaf.contains(supplier)) {
 		System.out.println("Closing supplier not in GIAF: " + supplier.getExternalId());
 		if (supplier.getTotalAllocated().isZero()) {
 		    System.out.println("Deleting supplier " + supplier.getExternalId());
 		    supplier.delete();
 		} else if (!supplier.getSupplierLimit().equals(Money.ZERO)) {
 		    supplier.setSupplierLimit(Money.ZERO);
 		}
 	    }
 	}
     }
 
     private static boolean shouldDiscard(final GiafSupplier giafSupplier) {
 	final String giafKey = giafSupplier.codEnt;
 	return (giafKey.length() == 6 && giafKey.charAt(0) == '1') || (giafKey.length() == 10 && giafKey.charAt(0) == 'E');
     }
 
     private static void updateSupplierInformation(final Supplier supplier, final GiafSupplier giafSupplier) {
 	if (giafSupplier.canceled || shouldDiscard(giafSupplier)) {
 	    if (giafSupplier.canceled) {
 		System.out.println("Closing canceled supplier: " + supplier.getExternalId());
 	    } else {
 		System.out.println("Closing discared supplier: " + supplier.getExternalId());
 	    }
 	    if (supplier.getTotalAllocated().isZero()) {
 		System.out.println("Deleting supplier " + supplier.getExternalId());
 		supplier.delete();
 	    } else {
 		updateSupplierInformationAux(supplier, giafSupplier);
 		if (!supplier.getSupplierLimit().equals(Money.ZERO)) {
 		    supplier.setSupplierLimit(Money.ZERO);
 		}
 	    }
 	} else {
 	    updateSupplierInformationAux(supplier, giafSupplier);
 	    if (supplier.getSupplierLimit().equals(Money.ZERO)) {
 		supplier.setSupplierLimit(Supplier.SOFT_SUPPLIER_LIMIT);
 	    }
 	}
     }
 
     private static void updateSupplierInformationAux(final Supplier supplier, final GiafSupplier giafSupplier) {
 	final String country = getCountry(giafSupplier);
 	if (!giafSupplier.numFis.equals(supplier.getFiscalIdentificationCode())) {
 	    supplier.setFiscalIdentificationCode(giafSupplier.numFis);
 	}
 	if (!giafSupplier.nom_ent.equals(supplier.getName())) {
 	    supplier.setName(giafSupplier.nom_ent);
 	}
 	if (!giafSupplier.nom_ent_abv.equals(supplier.getAbbreviatedName())) {
 	    supplier.setAbbreviatedName(giafSupplier.nom_ent_abv);
 	}
 	if (!giafSupplier.telEnt.equals(supplier.getPhone())) {
 	    supplier.setPhone(giafSupplier.telEnt);
 	}
 	if (!giafSupplier.faxEnt.equals(supplier.getFax())) {
 	    supplier.setFax(giafSupplier.faxEnt);
 	}
 	if (!giafSupplier.email.equals(supplier.getEmail())) {
 	    supplier.setEmail(giafSupplier.email);
 	}
 	final Address address = new Address(giafSupplier.ruaEnt, null, giafSupplier.codPos, giafSupplier.locEnt, country);
 	if (!address.equals(supplier.getAddress())) {
 	    supplier.setAddress(address);
 	}
     }
 
     private static Supplier findSupplierByGiafKey(final String codEnt) {
 	for (final Supplier supplier : ExpenditureTrackingSystem.getInstance().getSuppliersSet()) {
 	    final String giafKey = supplier.getGiafKey();
 	    if (codEnt.equals(giafKey)) {
 		return supplier;
 	    }
 	}
 	return null;
     }
 
     private static String getCountry(GiafSupplier giafSupplier) {
 	return giafSupplier.codPai != null && giafSupplier.codPai.equals("P") ? "Portugal" : "?";
     }
 
 }
