 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gov.bnl.racf.ps.dashboard3.domainobjects;
 
 
 import gov.bnl.racf.ps.dashboard3.parameters.PsParameters;
 import java.util.*;
 import javax.persistence.*;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.*;
 
 /**
  * Object describing perfSonar service matrix.
  *
  * @author tomw
  */
 @Cacheable
 @Entity
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 public class PsMatrix {
 
     // field names in JSON objects
     public static final String ID = "id";
     public static final String NAME = "name";
     public static final String DETAIL_LEVEL = "detailLevel";
     public static final String STATUS_LABELS = "statusLabels";
     public static final String LAST_UPDATE_TIME = "lastUpdateTime";
     public static final String NUMBER_OF_ROWS = "numberOfRows";
     public static final String ROWS = "rows";
     public static final String NUMBER_OF_COLUMNS = "numberOfColumns";
     public static final String COLUMNS = "columns";
     public static final String MAX_NUMBER_OF_SERVICE_NAMES = "maxNumberOfServiceNames";
     public static final String NUMBER_OF_SERVICE_NAMES = "numberOfServiceNames";
     public static final String MATRIX = "matrix";
     public static final String SERVICE_NAMES = "serviceNames";
     public static final String SERVICE_TYPE_ID = "serviceTypeId";
     
     // parameter names
     public static final String PARAMETER_SOURCE_HOST_ID="source-host-id";
     public static final String PARAMETER_DESTINATION_HOST_ID="destination-host-id";
     public static final String PARAMETER_MA_HOST_ID="ma-host-id";
     
     @Id
     @GeneratedValue
     private int id;
     private String name;
     private String detailLevel = "low";
 
     @CollectionOfElements
     private List<String> statusLabels = new ArrayList<String>();
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastUpdateTime;
     // max allowed matrix size
     private int maxNumberOfColumns = 60;
     private int maxNumberOfRows = 60;
     // 
     // rows
     @ManyToMany(cascade = CascadeType.ALL)
     @JoinTable(name = "PsMatrix_PsHostsInRows")
     @IndexColumn(name="idx")
     private List<PsHost> hostsInRows = new ArrayList<PsHost>();
     //
     // columns
     @ManyToMany(cascade = CascadeType.ALL)
     @JoinTable(name = "PsMatrix_PsHostsInColumns")
     @IndexColumn(name="idx")
     private List<PsHost> hostsInColumns = new ArrayList<PsHost>();
     //
     private int maxNumberOfServiceNames = 2;
 
     //@Lob
     //private String[] serviceNames = new String[2];
     @CollectionOfElements
     private List<String> serviceNames = new ArrayList<String>();
     //
     //
     @ManyToMany(cascade = CascadeType.ALL)
     private Collection<PsService> services = new Vector<PsService>();
     @Lob
     private int[][][] matrixOfServiceIds = new int[maxNumberOfColumns][maxNumberOfRows][maxNumberOfServiceNames];
     @ManyToOne
     private PsServiceType matrixType;
 
     /**
      * simple constructor, build empty matrix
      */
     public PsMatrix() {
         //initializeNumberOfServiceNames();
         initializeEmptyMatrix();
         //initializeStatusLabels();
     }
 
     /**
      * initialize empty matrix
      *
      * @param matrixType
      */
     public void initMatrix(PsServiceType matrixType) {
 
         this.matrixType = matrixType;
         initializeNumberOfServiceNames();
         initializeEmptyMatrix();
         initializeStatusLabels();
     }
 
     /**
      * construct empty matrix with a given name
      *
      * @param name
      */
     public PsMatrix(PsServiceType matrixType, String name) {
 
         this.matrixType = matrixType;
         this.name = name;
         initializeNumberOfServiceNames();
         initializeEmptyMatrix();
         initializeStatusLabels();
     }
 
     private void initializeNumberOfServiceNames() {
         if (this.matrixType.isTraceroute()) {
             this.serviceNames.add("Traceroute Check");
         }
         if (this.matrixType.isLatency()) {
             this.serviceNames.add("Packet Loss Check - Forward");
             this.serviceNames.add("Packet Loss Check - Reverse");
         }
         if (this.matrixType.isThroughput()) {
             this.serviceNames.add("Throughput Check - Forward");
             this.serviceNames.add("Throughput Check - Reverse");
         }
     }
 
 //     private void initializeNumberOfServiceNames() {
 //        if (this.matrixType.isTraceroute()) {
 //            this.numberOfServiceNames = 1;
 //            this.serviceNames[0] = "Traceroute Check";
 //        }
 //        if (this.matrixType.isLatency()) {
 //            this.numberOfServiceNames = 2;
 //            this.serviceNames[0] = "Packet Loss Check - Forward";
 //            this.serviceNames[1] = "Packet Loss Check - Reverse";
 //        }
 //        if (this.matrixType.isThroughput()) {
 //            this.numberOfServiceNames = 2;
 //            this.serviceNames[0] = "Throughput Check - Forward";
 //            this.serviceNames[1] = "Throughput Check - Reverse";
 //        }
 //    }
 //    public String[] getServiceNames() {
 //        return serviceNames;
 //    }
 //    public void setServiceNames(String[] serviceNames) {
 //        this.serviceNames = serviceNames;
 //    }
     private void initializeStatusLabels() {
         if (PsParameters.THROUGHPUT.equals(matrixType.getServiceTypeId())) {
             statusLabels.add("Throughput > 500Mbps");
             statusLabels.add("Throughput > 100Mbps and < 500 Mbps");
             statusLabels.add("Throughput < 100Mbps");
             statusLabels.add("Server Error Occurred");
             statusLabels.add("Timeout error occurred");
         } else {
             statusLabels.add("OK");
             statusLabels.add("WARNING");
             statusLabels.add("CRITICAL");
             statusLabels.add("Server error occured");
             statusLabels.add("Timeout error occurred");
         }
     }
 
 
     public int getId() {
         return id;
     }
 
     public List<PsHost> getAllHosts(){
         List<PsHost> allHostsInThisMatrix = new ArrayList<PsHost>();
         
         Iterator iter = getHostsInRows().iterator();
         while(iter.hasNext()){
             PsHost currentHost = (PsHost)iter.next();
             if(!allHostsInThisMatrix.contains(currentHost) ){
                 allHostsInThisMatrix.add(currentHost);
             }
         }
         
         iter = getHostsInColumns().iterator();
         while(iter.hasNext()){
             PsHost currentHost = (PsHost)iter.next();
             if(!allHostsInThisMatrix.contains(currentHost) ){
                 allHostsInThisMatrix.add(currentHost);
             }
         }
         
         return allHostsInThisMatrix;
     }
     
     public List<PsHost> getHostsInColumns() {
         return hostsInColumns;
     }
 
     public void setHostsInColumns(List<PsHost> hostsInColumns) {
         this.hostsInColumns = hostsInColumns;
     }
 
     public List<PsHost> getHostsInRows() {
         return hostsInRows;
     }
 
     public void setHostsInRows(List<PsHost> hostsInRows) {
         this.hostsInRows = hostsInRows;
     }
 
     public PsServiceType getMatrixType() {
         return matrixType;
     }
 
     public void setMatrixType(PsServiceType matrixType) {
         this.matrixType = matrixType;
     }
 
     public String getDetailLevel() {
         return detailLevel;
     }
 
     public void setDetailLevel(String detailLevel) {
         this.detailLevel = detailLevel;
     }
 
     public Date getLastUpdateTime() {
         return lastUpdateTime;
     }
 
     public void setLastUpdateTime(Date lastUpdateTime) {
         this.lastUpdateTime = lastUpdateTime;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public int getNumberOfColumns() {
         return hostsInColumns.size();
     }
 
     public int getNumberOfRows() {
         return hostsInRows.size();
     }
 
     /**
      * set matrix element
      *
      * @param rowIndex
      * @param columnIndex
      * @param serviceIndex
      * @param service
      */
     public void setMatrixElement(
             int rowIndex, int columnIndex, int serviceIndex, PsService service) {
         int serviceId = service.getId();
         this.matrixOfServiceIds[rowIndex][columnIndex][serviceIndex] = serviceId;
 
     }
 
     /**
      * get matrix type
      *
      * @return
      */
     public PsServiceType getType() {
         return matrixType;
     }
 
     /**
      * get column number for host
      *
      * @param host
      * @return
      */
     public int getColumnNumberOfHost(PsHost host) {
         return getColumnNumberOfHost(host.getId());
     }
 
     public int getColumnNumberOfHost(int hostId) {
         int columnNotFound = -1;
         int column = 0;
         Iterator iter = hostsInColumns.iterator();
         while (iter.hasNext()) {
             PsHost currentHost = (PsHost) iter.next();
             if (hostId == currentHost.getId()) {
                 return column;
             }
             column = column + 1;
         }
         return columnNotFound;
     }
 
     /**
      * get row number of a host
      *
      * @param host
      * @return
      */
     public int getRowNumberOfHost(PsHost host) {
         return getRowNumberOfHost(host.getId());
     }
 
     public int getRowNumberOfHost(int hostId) {
         int rowNotFound = -1;
         int row = 0;
         Iterator iter = hostsInRows.iterator();
         while (iter.hasNext()) {
             PsHost currentHost = (PsHost) iter.next();
             if (currentHost.getId() == hostId) {
                 return row;
             }
             row = row + 1;
         }
         return rowNotFound;
     }
 
     public List<String> getServiceNames() {
         return serviceNames;
     }
 
     public void setServiceNames(List<String> serviceNames) {
         this.serviceNames = serviceNames;
     }
 
     public List<String> getStatusLabels() {
         return statusLabels;
     }
 
     public void setStatusLabels(List<String> statusLabels) {
         this.statusLabels = statusLabels;
     }
 
 //    public String[] getStatusLabels() {
 //        return statusLabels;
 //    }
 //
 //    public void setStatusLabels(String[] statusLabels) {
 //        this.statusLabels = statusLabels;
 //    }
     public int getNumberOfServiceNames() {
         return this.serviceNames.size();
     }
 
     /**
      * Initialize matrix, fill its elements with null
      */
     private void initializeEmptyMatrix() {
 
         for (int i = 0; i < maxNumberOfColumns; i = i + 1) {
             for (int j = 0; j < maxNumberOfRows; j = j + 1) {
                 for (int k = 0; k < maxNumberOfServiceNames; k = k + 1) {
                     matrixOfServiceIds[i][j][k] = -1;
                 }
             }
         }
     }
 
     /**
      * check if matrix contains host in column
      *
      * @param host
      * @return
      */
     public boolean containsHostInColumn(PsHost host) {
         return containsHostInColumn(host.getId());
     }
 
     /**
      * check if matrix contains host in columns
      *
      * @param hostId
      * @return
      */
     public boolean containsHostInColumn(int hostId) {
         int columnNumberOfThisHost = this.getColumnNumberOfHost(hostId);
         if (columnNumberOfThisHost == -1) {
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * check if matrix contains host in row
      *
      * @param host
      * @return
      */
     public boolean containsHostInRow(PsHost host) {
         return containsHostInRow(host.getId());
     }
 
     /**
      * check if matrix contains host in row
      *
      * @param hostId
      * @return
      */
     public boolean containsHostInRow(int hostId) {
         int rowNumberOfThisHost = this.getRowNumberOfHost(hostId);
         if (rowNumberOfThisHost == -1) {
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * check if matrix contains host hostId
      *
      * @param hostId
      * @return
      */
     public boolean containsHost(int hostId) {
         boolean result = false;
         boolean inColumn = containsHostInColumn(hostId);
         if (inColumn) {
             return true;
         }
 
         boolean inRow = containsHostInRow(hostId);
         if (inRow) {
             return true;
         }
         return result;
     }
 
     /**
      * check if matrix contains host
      *
      * @param host
      * @return
      */
     public boolean containsHost(PsHost host) {
         return containsHost(host.getId());
     }
 
     /**
      * add host to columns
      *
      * @param host
      */
     public void addHostToColumn(PsHost host) {
         if (!containsHostInColumn(host)) {
             hostsInColumns.add(host);
         }
     }
 
     /**
      * add host to rows
      *
      * @param host
      */
     public void addHostToRow(PsHost host) {
         if (!containsHostInRow(host)) {
             hostsInRows.add(host);
         }
     }
 
     /**
      * add new host to matrix. This method does not fill the matrix with the
      * corresponding services. This has to be done separately by the matrix
      * manipulator
      *
      * @param host
      */
     public void addHost(PsHost host) {
         addHostToColumn(host);
         addHostToRow(host);
     }
 
     /**
      * remove host from matrix Shrink the matrix accordingly
      *
      * @param host
      */
     public List<PsService> removeHost(PsHost host) {
         return removeHost(host.getId());
     }
 
     /**
      * remove host from matrix. Shrink matrix as needed
      *
      * @param hostId
      */
     public List<PsService> removeHost(int hostId) {
         List<PsService> servicesToBeRemoved = new ArrayList<PsService>();
 
         List<PsService> servicesDeletedFromColumn = removeHostFromColumns(hostId);
         if (!servicesDeletedFromColumn.isEmpty()) {
             servicesToBeRemoved.addAll(servicesDeletedFromColumn);
         }
 
         List<PsService> servicesDeletedFromRow = removeHostFromRows(hostId);
         if (!servicesDeletedFromRow.isEmpty()) {
             servicesToBeRemoved.addAll(servicesDeletedFromRow);
         }
         return servicesToBeRemoved;
     }
 
     /**
      * remove host hostId from columns
      *
      * @param hostId
      */
     public List<PsService> removeHostFromColumns(int hostId) {
         List<PsService> servicesFromThisColumn = new ArrayList<PsService>();
         if (this.containsHostInColumn(hostId)) {
             int columnNumberOfThisHost = this.getColumnNumberOfHost(hostId);
             servicesFromThisColumn = removeColumn(columnNumberOfThisHost);
         }
         return servicesFromThisColumn;
     }
 
     /**
      * remove host hostId from rows
      *
      * @param hostId
      */
     public List<PsService> removeHostFromRows(int hostId) {
         List<PsService> servicesToBeRemoved = new ArrayList<PsService>();
         if (this.containsHostInRow(hostId)) {
             int rowNumberOfThisHost = this.getRowNumberOfHost(hostId);
             servicesToBeRemoved = removeRow(rowNumberOfThisHost);
         }
         return servicesToBeRemoved;
     }
 
     /**
      * remove columnNo from matrix
      *
      * @param columnNo
      */
     public List<PsService> removeColumn(int columnNo) {
         List<PsService> servicesRemoved = new ArrayList<PsService>();
 
         for (int columnIndex = columnNo; columnIndex < getNumberOfColumns();
                 columnIndex = columnIndex + 1) {
 
             for (int j = 0; j < getNumberOfRows(); j = j + 1) {
                 for (int k = 0; k < maxNumberOfServiceNames; k = k + 1) {
                     int serviceId = matrixOfServiceIds[columnIndex][j][k];
                     if (serviceId != -1) {
                         PsService serviceToBeRemoved = removeServiceFromList(serviceId);
                         if (serviceToBeRemoved != null) {
                             servicesRemoved.add(serviceToBeRemoved);
                         }
                     }
                     matrixOfServiceIds[columnIndex][j][k] = matrixOfServiceIds[columnIndex + 1][j][k];
                     matrixOfServiceIds[columnIndex + 1][j][k] = -1;
                 }
             }
         }
         // ok, now remove host from list of hosts in column
         this.getHostsInColumns().remove(columnNo);
 
         return servicesRemoved;
     }
 
     /**
      * remove row from matrix
      *
      * @param rowNo
      */
     public List<PsService> removeRow(int rowNo) {
         List<PsService> servicesRemoved = new ArrayList<PsService>();
 
 
 
         for (int rowIndex = rowNo; rowIndex < getNumberOfRows(); rowIndex = rowIndex + 1) {
 
             for (int i = 0; i < this.getNumberOfColumns(); i = i + 1) {
                 for (int k = 0; k < maxNumberOfServiceNames; k = k + 1) {
 
                     int serviceId = matrixOfServiceIds[i][rowIndex][k];
                     if (serviceId != -1) {
                         PsService serviceToBeRemoved = removeServiceFromList(serviceId);
                         if (serviceToBeRemoved != null) {
                             servicesRemoved.add(serviceToBeRemoved);
                         }
                     }
                     matrixOfServiceIds[i][rowIndex][k] = matrixOfServiceIds[i][rowIndex + 1][k];
                     matrixOfServiceIds[i][rowIndex + 1][k] = -1;
 
                 }
             }
         }
         // remove host from list of hosts in row
         this.getHostsInRows().remove(rowNo);
         return servicesRemoved;
     }
 
     /**
      * add service no k into designated column and row
      *
      * @param hostColumn
      * @param hostRow
      * @param k
      * @param service
      */
     public void addService(PsHost hostColumn,
             PsHost hostRow,
             int k,
             PsService service) {
         addService(hostColumn.getId(), hostRow.getId(), k, service);
     }
 
     /**
      * add service to matrix in column and row. Use service number k.
      *
      */
     public void addService(int column,
             int row,
             int k,
             PsService service) {
         
         if (column>-1 && column < this.getNumberOfColumns() &&
                 row> -1 && row<this.getNumberOfRows()) {
             services.add(service);
             matrixOfServiceIds[column][row][k] = service.getId();
         }
 
     }
 
     /**
      * get service from designated column and row. If there is more than one
      * service in that location choose service serviceNo
      *
      * @param columnHost
      * @param rowHost
      * @param serviceNo
      * @return
      */
     public PsService getService(PsHost columnHost,
             PsHost rowHost, int serviceNo) {
         return getService(columnHost.getId(), rowHost.getId(), serviceNo);
 
     }
 
    
     /**
     * get service which stands in matrix in a given column and row and given service number.
     * @param column
     * @param row
     * @param serviceNo
     * @return 
      */
     public PsService getService(int column, int row,  int serviceNo) {
 
         if (column>-1 && column<this.getNumberOfColumns() &&
                 row> -1 && row<this.getNumberOfRows()) {
            //int serviceId = matrixOfServiceIds[row][column][serviceNo];
            // fixed bug following the non-spring dashboard
            int serviceId = matrixOfServiceIds[column][row][serviceNo];
             return getServiceById(serviceId);
         } else {
             return null;
         }
     }
 
     /**
     * return service from this matrix by Id. Null if service not found.
      *
      * @param serviceId
      * @return
      */
     public PsService getServiceById(int serviceId) {
         Iterator iter = services.iterator();
         while (iter.hasNext()) {
             PsService currentService = (PsService) iter.next();
             if (currentService.getId() == serviceId) {
                 return currentService;
             }
         }
         return null;
     }
 
     /**
      * get element of service matrix corresponding to columnId, rowId and
      * serviceId (serviceId is the number of service in matrix, not the service
      * identifier!!!)
      *
      * @param columnId
      * @param rowId
      * @param serviceId
      * @return
      */
     public PsService getServiceByRowAndColumnNumber(int columnId, int rowId, int serviceId) {
         if (columnId != -1 && rowId != -1) {
             int serviceIdentifier = matrixOfServiceIds[columnId][rowId][serviceId];
             return getServiceById(serviceIdentifier);
         } else {
             return null;
         }
     }
 
     /**
      * get host stored at location rowIndex in rows table
      *
      * @param rowIndex
      * @return
      */
     public PsHost getHostInRow(int rowIndex) {
         if (rowIndex < 0 || rowIndex > getNumberOfRows() - 1) {
             return null;
         } else {
             return this.hostsInRows.get(rowIndex);
         }
     }
 
     /**
      * get host stored at location columnIndex in array columns
      *
      * @param columnIndex
      * @return
      */
     public PsHost getHostInColumn(int columnIndex) {
         if (columnIndex < 0 || columnIndex > getNumberOfColumns() - 1) {
             return null;
         } else {
             return hostsInColumns.get(columnIndex);
         }
     }
 
     /**
      * remove service serviceId from the matrix
      *
      * @param serviceId
      */
     public List<PsService> removeService(int serviceId) {
         List<PsService> servicesRemoved = new ArrayList<PsService>();
         if (serviceId != -1) {
             for (int i = 0; i < maxNumberOfColumns; i = i + 1) {
                 for (int j = 0; j < maxNumberOfRows; j = j + 1) {
                     for (int k = 0; k < maxNumberOfServiceNames; k = k + 1) {
                         if (matrixOfServiceIds[i][j][k] != -1) {
                             if (matrixOfServiceIds[i][j][k] == serviceId) {
                                 matrixOfServiceIds[i][j][k] = -1;
                                 PsService serviceRemoved = removeServiceFromList(serviceId);
                                 if (serviceRemoved != null) {
                                     servicesRemoved.add(serviceRemoved);
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return servicesRemoved;
     }
 
     /**
      * return a Vector containing all service Id's from this matrix
      *
      * @return
      */
     public Vector<Integer> getComponentServiceIds() {
         Vector<Integer> componentServiceIds = new Vector<Integer>();
         Iterator iter = this.services.iterator();
         while (iter.hasNext()) {
             PsService currentService = (PsService) iter.next();
             Integer serviceIdInteger = new Integer(currentService.getId());
             componentServiceIds.add(serviceIdInteger);
         }
         return componentServiceIds;
     }
 
     /**
      * check if this is throughput matrix
      *
      * @return
      */
     public boolean isThroughput() {
         return matrixType.isThroughput();
     }
 
     /**
      * check if this is latency matrix
      *
      * @return
      */
     public boolean isLatency() {
         return matrixType.isLatency();
     }
 
     /**
      * check if this is traceroute matrix
      *
      * @return
      */
     public boolean isTraceroute() {
         return matrixType.isTraceroute();
     }
 
     /**
      * get vector containing service id's of services from a given column
      *
      * @param columnNumber
      * @return
      */
     public Vector<Integer> getServiceIdsInColumn(int columnNumber) {
         Vector<Integer> listOfServiceIds = new Vector<Integer>();
         if (columnNumber > -1 && columnNumber < getNumberOfColumns()) {
             for (int row = 0; row < getNumberOfRows(); row = row + 1) {
                 for (int serviceName = 0; 
                         serviceName < this.getNumberOfServiceNames(); 
                         serviceName = serviceName + 1) {
                     if (matrixOfServiceIds[columnNumber][row][serviceName] != -1) {
                         Integer serviceIdInteger = 
                                 new Integer(matrixOfServiceIds[columnNumber][row][serviceName]);
                         listOfServiceIds.add(serviceIdInteger);
                     }
                 }
             }
         }
         return listOfServiceIds;
     }
 
     /**
      * get vector containing service id's of services from a given row
      *
      * @param columnNumber
      * @return
      */
     public Vector<Integer> getServiceIdsInRow(int rowNumber) {
         Vector<Integer> listOfServiceIds = new Vector<Integer>();
         if (rowNumber > -1 && rowNumber < getNumberOfRows()) {
             for (int column = 0; column < getNumberOfColumns(); column = column + 1) {
                 for (int serviceName = 0; 
                         serviceName < this.getNumberOfServiceNames(); 
                         serviceName = serviceName + 1) {
                     if (matrixOfServiceIds[column][rowNumber][serviceName] != -1) {
                         int serviceId = matrixOfServiceIds[column][rowNumber][serviceName];
                         Integer serviceIdInteger = new Integer(serviceId);
                         listOfServiceIds.add(serviceIdInteger);
                     }
                 }
             }
         }
         return listOfServiceIds;
     }
 
     /**
      * take id of a service, remove service with this id from list of services
      * in the matrix. Return this service object. If service is not found return
      * null
      *
      * @param serviceId
      * @return
      */
     public PsService removeServiceFromList(int serviceId) {
         Iterator iter = services.iterator();
         while (iter.hasNext()) {
             PsService currentService = (PsService) iter.next();
             if (currentService.getId() == serviceId) {
                 iter.remove();
                 return currentService;
             }
         }
         return null;
     }
 }
