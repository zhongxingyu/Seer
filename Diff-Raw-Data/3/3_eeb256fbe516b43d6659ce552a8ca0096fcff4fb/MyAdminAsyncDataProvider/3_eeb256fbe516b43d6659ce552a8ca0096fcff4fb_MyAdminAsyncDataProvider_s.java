 /**
  * Copyright (c) 2013 Christian Pelster.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Christian Pelster - initial API and implementation
  */
 package de.pellepelster.myadmin.client.gwt.modules.dictionary;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.Range;
 
 import de.pellepelster.myadmin.client.base.db.vos.IBaseVO;
 import de.pellepelster.myadmin.client.base.jpql.GenericFilterVO;
 import de.pellepelster.myadmin.client.web.MyAdmin;
 import de.pellepelster.myadmin.client.web.util.SimpleCallback;
 
 public class MyAdminAsyncDataProvider<VOType extends IBaseVO> extends AsyncDataProvider<VOType>
 {
 	private GenericFilterVO<VOType> genericFilterVO;
 
 	private SimpleCallback<Integer> resultsChangedCallback;
 
 	private void callResultsChangedCallback(int resultsCount)
 	{
 		if (resultsChangedCallback != null)
 		{
 			resultsChangedCallback.onCallback(resultsCount);
 		}
 	}
 
 	private void getData(final Range range)
 	{
 
 		final int start = range.getStart();
 
 		if (genericFilterVO != null)
 		{
 			genericFilterVO.setFirstResult(start);
 			genericFilterVO.setMaxResults(range.getLength());
 
 			MyAdmin.getInstance().getRemoteServiceLocator().getBaseEntityService().filter(genericFilterVO, new AsyncCallback<List<VOType>>()
 			{
 
 				/** {@inheritDoc} */
 				@Override
 				public void onFailure(Throwable caught)
 				{
 					updateRowData(start, new ArrayList<VOType>());
 				}
 
 				@Override
 				public void onSuccess(List<VOType> result)
 				{
 					updateRowData(start, result);
 					callResultsChangedCallback(result.size());
 				}
 			});
 		}
 		else
 		{
 			updateRowData(start, new ArrayList<VOType>());
 			updateRowCount(0, true);
 			callResultsChangedCallback(0);
 		}
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	protected void onRangeChanged(HasData<VOType> display)
 	{
 		final Range range = display.getVisibleRange();
 		getData(range);
 	}
 
 	public void setGenericFilterVO(GenericFilterVO<VOType> genericFilterVO)
 	{
 		this.genericFilterVO = genericFilterVO;
 	}
 
 	public void setResultsChangedCallback(SimpleCallback<Integer> resultsChangedCallback)
 	{
 		this.resultsChangedCallback = resultsChangedCallback;
 	}
 
 }
