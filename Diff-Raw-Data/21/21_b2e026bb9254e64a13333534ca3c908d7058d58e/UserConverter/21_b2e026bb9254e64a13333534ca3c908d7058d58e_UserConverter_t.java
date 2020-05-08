 package com.maxtorzito.shared.converters;
 
 import com.maxtorzito.shared.model.User;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.convert.FacesConverter;
 
 @FacesConverter(value = "userConverter")
public class UserConverter implements Converter {
 
     @Override
     public Object getAsObject(FacesContext fc, UIComponent uic, String userName) throws ConverterException {
        System.out.println("username:" + userName);
        if (userName != null) {
            return new User(userName);
        }
        return null;
     }
 
     @Override
     public String getAsString(FacesContext fc, UIComponent uic, Object o) throws ConverterException {
        if (o != null && o instanceof User) {
            User user = (User) o;
            return user.getUserName();
        }
        return null;
    }
 }
