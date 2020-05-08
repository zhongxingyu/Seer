 package com.polopoly.ps.tools.collections.incontent;
 
 import static com.polopoly.util.Require.require;
 import static com.polopoly.util.policy.Util.util;
 
 import java.util.Iterator;
 
 import com.polopoly.cm.client.CMException;
 import com.polopoly.cm.policy.Policy;
 import com.polopoly.ps.tools.collections.exception.NoSuchCollectionException;
 import com.polopoly.ps.tools.collections.exception.NoSuchEntryException;
 import com.polopoly.util.content.ContentUtil;
 import com.polopoly.util.exception.PolicyModificationException;
 import com.polopoly.util.policy.PolicyModification;
 import com.polopoly.util.policy.PolicyUtil;
 
 public class DefaultMapInContent<W> implements MapInContent<W> {
 
 	protected ComponentCollectionProvider<W> provider;
 	protected PolicyUtil policy;
 	protected ContentUtil content;
 
 	public DefaultMapInContent(PolicyUtil policy,
 			ComponentCollectionProvider<W> provider) {
 		this.provider = require(provider);
 		this.policy = require(policy);
 		this.content = policy.getContent();
 	}
 
 	@Override
 	public DefaultMapInContent<W> modify(
 			final PolicyModification<EditableMapInContent<W>> modification)
 			throws PolicyModificationException {
 		Policy result = policy.modify(new PolicyModification<Policy>() {
 			@Override
 			public void modify(Policy newVersion) throws CMException {
 				modification.modify(new DefaultEditableMapInContent<W>(
 						util(newVersion), provider));
 			}
 		}, Policy.class);
 
 		return new DefaultMapInContent<W>(util(result), provider);
 	}
 
 	@Override
 	public W get(String key) throws NoSuchEntryException {
 		try {
 			return provider.get(key, content);
 		} catch (NoSuchCollectionException e) {
 			throw new NoSuchEntryException(e);
 		}
 	}
 
 	@Override
 	public Iterator<W> iterator() {
 		return (Iterator<W>) provider.values(content);
 	}
 
 	@Override
 	public Iterator<String> keys() {
 		return provider.keys(content);
 	}
 
 }
