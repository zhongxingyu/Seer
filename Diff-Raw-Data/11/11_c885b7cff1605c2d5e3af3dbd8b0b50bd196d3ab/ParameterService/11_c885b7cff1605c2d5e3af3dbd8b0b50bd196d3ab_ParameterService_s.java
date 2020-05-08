 package cn.seu.cose.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.stereotype.Service;
 
 import cn.seu.cose.dao.ParameterDAOImpl;
 import cn.seu.cose.entity.Parameter;
 
 @Service
 public class ParameterService {
 
 	@Autowired
 	ParameterDAOImpl parameterDAOImpl;
 
 	public List<Parameter> getAllParameters() {
 		return parameterDAOImpl.getAllParameters();
 	}
 
 	@Cacheable(value = "parameterCache", key = "'getParameterByKey:'+#key")
 	public Parameter getParameterByKey(String key) {
 		return parameterDAOImpl.getParameterByKey(key);
 	}
 
	@CacheEvict(value = "parameterCache")
 	public void updateParameterValue(Parameter parameter, String value) {
 		parameter.setParameterValue(value);
 		parameterDAOImpl.updateParameter(parameter);
 	}
 
	@CacheEvict(value = "parameterCache")
 	public void updateParameterValue(Parameter parameter) {
 		parameterDAOImpl.updateParameter(parameter);
 	}
 
	@CacheEvict(value = "parameterCache")
 	public void updateParameterValue(String key, String value) {
 		Parameter parameter = parameterDAOImpl.getParameterByKey(key);
 		if (parameter != null) {
 			parameter.setParameterValue(value);
 			parameterDAOImpl.updateParameter(parameter);
 		}
 	}
 	
	@CacheEvict(value = "parameterCache")
 	public void updateParameterExtra(String key, String extra) {
 		Parameter parameter = parameterDAOImpl.getParameterByKey(key);
 		if (parameter != null) {
 			parameter.setExtra(extra);
 			parameterDAOImpl.updateParameter(parameter);
 		}
 	}
 }
