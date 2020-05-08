 package com.maxtorzito.shared.converters;
 

 import com.maxtorzito.shared.model.User;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.convert.FacesConverter;
 
 @FacesConverter(value = "userConverter")
public class UserConverter implements Converter{
 
     @Override
     public Object getAsObject(FacesContext fc, UIComponent uic, String userName) throws ConverterException {
        System.out.println("username:"+userName);
        return new User(userName);
     }
 
     @Override
     public String getAsString(FacesContext fc, UIComponent uic, Object o) throws ConverterException {
        return ((User)o).getUserName();
    }   
 }
