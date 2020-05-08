 package org.gwaspi.dao;
 
 import java.io.IOException;
 import java.util.List;
 import org.gwaspi.model.Operation;
 import org.gwaspi.model.Report;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public interface ReportService {
 
 	Report getById(int reportId) throws IOException;
 
	// XXX split into two methods, with either parentOperationId or parentMatrixId param, as we do not allow to fetch by specifying both anyway (on has to be Integer.MIN_VALUE as it works now)
 	List<Report> getReportsList(int parentOperationId, int parentMatrixId) throws IOException;
 
 	String getReportNamePrefix(Operation op);
 
 	String createReportsMetadataTable();
 
 	void insertRPMetadata(Report report) throws IOException;
 
 	void deleteReportByMatrixId(int parentMatrixId) throws IOException;
 
 	void deleteReportByOperationId(int parentOperationId) throws IOException;
 }
