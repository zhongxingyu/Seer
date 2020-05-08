 package com.photon.phresco.service.converters;
 
 import org.springframework.data.document.mongodb.MongoOperations;
 
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.dao.CustomerDAO;
 
 public class CustomerConverter implements Converter<CustomerDAO, Customer> {
 
 	@Override
 	public Customer convertDAOToObject(CustomerDAO dao,
 			MongoOperations mongoOperation) throws PhrescoException {
 		Customer customer = new Customer();
 		customer.setAddress(dao.getAddress());
 		customer.setApplicableTechnologies(dao.getApplicableTechnologies());
 		customer.setContactNumber(dao.getContactNumber());
 		customer.setCountry(dao.getCountry());
 		customer.setCreationDate(dao.getCreationDate());
 		customer.setDescription(dao.getDescription());
 		customer.setEmailId(dao.getEmailId());
 		customer.setFax(dao.getFax());
 		customer.setHelpText(dao.getHelpText());
 		customer.setId(dao.getId());
 		customer.setName(dao.getName());
 		customer.setRepoInfo(dao.getRepoInfo());
 		customer.setState(dao.getState());
 		customer.setStatus(dao.getStatus());
 		customer.setType(dao.getType());
 		customer.setValidFrom(dao.getValidFrom());
 		customer.setValidUpto(dao.getValidUpto());
 		customer.setZipcode(dao.getZipcode());
 		customer.setFrameworkTheme(dao.getFrameworkTheme());
 		return customer;
 	}
 
 	@Override
 	public CustomerDAO convertObjectToDAO(Customer customer)
 			throws PhrescoException {
 		CustomerDAO customerDAO = new CustomerDAO();
 		customerDAO.setAddress(customer.getAddress());
 		customerDAO.setApplicableTechnologies(customer.getApplicableTechnologies());
 		customerDAO.setContactNumber(customer.getContactNumber());
 		customerDAO.setCountry(customer.getCountry());
 		customerDAO.setCreationDate(customer.getCreationDate());
 		customerDAO.setDescription(customer.getDescription());
 		customerDAO.setEmailId(customer.getEmailId());
 		customerDAO.setFax(customer.getFax());
 		customerDAO.setHelpText(customer.getHelpText());
 		customerDAO.setId(customer.getId());
 		customerDAO.setName(customer.getName());
 		customerDAO.setRepoInfo(customer.getRepoInfo());
 		customerDAO.setState(customer.getState());
 		customerDAO.setStatus(customer.getStatus());
 		customerDAO.setType(customer.getType());
 		customerDAO.setValidFrom(customer.getValidFrom());
 		customerDAO.setValidUpto(customer.getValidUpto());
 		customerDAO.setZipcode(customer.getZipcode());
 		customerDAO.setFrameworkTheme(customer.getFrameworkTheme());
 		return customerDAO;
 	}
 
 }
