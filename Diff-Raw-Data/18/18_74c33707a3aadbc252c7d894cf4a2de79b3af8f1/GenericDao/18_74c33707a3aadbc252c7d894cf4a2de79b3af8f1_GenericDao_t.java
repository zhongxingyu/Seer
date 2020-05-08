 package br.com.unifacs.dao;
 
 import java.io.Serializable;
 import java.util.List;
 
 public interface GenericDao<T, PK extends Serializable> {
 	
 	void inserir(T model)throws DaoException;
 	void atualizar(T model) throws DaoException;
 	void excluir(T model) throws DaoException;
 	T obter(PK id);
 	List<T> obterTodos();
	List<T> query(String q, String... param) throws DaoException;
 
 }
