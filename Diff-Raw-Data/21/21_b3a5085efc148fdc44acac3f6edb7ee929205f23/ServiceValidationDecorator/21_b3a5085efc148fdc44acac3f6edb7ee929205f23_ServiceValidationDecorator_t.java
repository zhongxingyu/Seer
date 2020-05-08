 package fr.esiea.esieaddress.service.validation;
 
 import fr.esiea.esieaddress.dao.exception.DaoException;
 import fr.esiea.esieaddress.model.Contact;
 import fr.esiea.esieaddress.service.crud.ICrudService;
 import fr.esiea.esieaddress.service.exception.ServiceException;
 import fr.esiea.esieaddress.service.validation.exception.ValidationException;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Copyright (c) 2013 ESIEA M. Labusquiere D. Déïs
  * <p/>
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * <p/>
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * <p/>
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 @Service
 public class ServiceValidationDecorator implements ICrudService<Contact> {
     private static final Logger LOGGER = Logger.getLogger(ServiceValidationDecorator.class);
     @Autowired
     @Qualifier("contactCrudService")
     private ICrudService<Contact> crudService;
 
     @Autowired
     private IValidationService validationService;
 
     @Override
     public Collection<Contact> getAll() throws ServiceException, DaoException {
         return crudService.getAll();
     }
 
     @Override
     public void remove(String idContact) throws ServiceException, DaoException {
         crudService.remove(idContact);
     }
 
     @Override
     public void save(Contact contact) throws ServiceException, DaoException {
         LOGGER.info("[VALIDATION] Do the validation on updatedContact contact " + contact.getId());
 
         final Map<Object, String> errorMap = validationService.validate(contact);
 
         if(errorMap.isEmpty())
             crudService.save(contact);
        else
            throw new ValidationException(errorMap);
 
     }
 
     @Override
     public void insert(Contact contact) throws ServiceException, DaoException {
         LOGGER.info("[VALIDATION] Do the validation on a new contact");
 
         final Map<Object,String> errorMaps = validationService.validate(contact);
 
         if(errorMaps.isEmpty())
             crudService.insert(contact);
        else
            throw new ValidationException(errorMaps);
     }
 
     @Override
     public Contact getOne(String contactId) throws ServiceException, DaoException {
         return crudService.getOne(contactId);
     }
 
 }
