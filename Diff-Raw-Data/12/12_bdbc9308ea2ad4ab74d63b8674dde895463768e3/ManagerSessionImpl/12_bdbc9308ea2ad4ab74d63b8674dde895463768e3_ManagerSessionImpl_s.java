 package rental.session;
 
 import java.rmi.RemoteException;
 import java.util.Collection;
import java.util.Map;
import java.util.Set;
 
 import rental.Agency;
 import rental.CarType;
 import rental.Company;
import rental.CompanyRegistry;
 
 public class ManagerSessionImpl extends Session implements ManagerSession {
 
 	public ManagerSessionImpl(Agency agency, String managerName) {
 		super(agency, managerName);
 	}
 
 	@Override
 	public Collection<Company> getAllCompanies() throws RemoteException {
 		return getAgency().getAllCompanies();
 	}
 
 	@Override
 	public void registerCompany(String companyName, Company company)
 			throws RemoteException {
 		getAgency().registerCompany(companyName, company);
 	}
 
 	@Override
 	public void unregisterCompany(String companyName) throws RemoteException {
 		getAgency().unregisterCompany(companyName);
 	}
 
 	@Override
 	public int getNumberOfReservationsBy(String clientName)
 			throws RemoteException {
 		int nbReservations = 0;
 		for (Company company : getAgency().getAllCompanies()) {
 			nbReservations += company.getNumberOfReservationsBy(clientName);
 		}
 		return nbReservations;
 	}
 
 	@Override
 	public int getNumberOfReservationsForCarType(String carRentalCompanyName,
 			String carType) throws RemoteException {
 		Company company = getAgency().getCompany(carRentalCompanyName);
 		return company.getNumberOfReservationsForCarType(carType);
 	}
 
 	@Override
 	public String getMostPopularCarRentalCompany() throws RemoteException {
 		String mostPopular = null;
 		int maxReservations = Integer.MIN_VALUE;
		for (Map.Entry<String, Company> entry : getAgency().getAllCompanies()
				.entrySet()) {
			String companyName = entry.getKey();
			int nbReservations = entry.getValue().getNumberOfReservations();
 			if (nbReservations > maxReservations) {
 				mostPopular = companyName;
 				maxReservations = nbReservations;
 			}
 		}
 		return mostPopular;
 	}
 
 	@Override
 	public CarType getMostPopularCarTypeIn(String carRentalCompanyName)
 			throws RemoteException {
 		CarType mostPopular = null;
 		int maxReservations = Integer.MIN_VALUE;
 		Company company = getAgency().getCompany(carRentalCompanyName);
 		for (CarType carType : company.getAllCarTypes()) {
 			int nbReservations = company
 					.getNumberOfReservationsForCarType(carType.getName());
 			if (nbReservations > maxReservations) {
 				mostPopular = carType;
 				maxReservations = nbReservations;
 			}
 		}
 		return mostPopular;
 	}
 
 }
